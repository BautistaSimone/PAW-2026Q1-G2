package ar.edu.itba.paw.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.Locale;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.PurchaseDao;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private static final long RESERVATION_MINUTES = 5L;

    private final PurchaseDao purchaseDao;
    private final ProductService productService;
    private final UserService userService;
    private final EmailService emailService;
    private final MessageSource messageSource;

    @Autowired
    public PurchaseServiceImpl(
            final PurchaseDao purchaseDao,
            final ProductService productService,
            final UserService userService,
            final EmailService emailService,
            final MessageSource messageSource) {
        this.purchaseDao = purchaseDao;
        this.productService = productService;
        this.userService = userService;
        this.emailService = emailService;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional
    public Purchase createPurchase(Long productId, Long userId) {
        if (productId == null) {
            throw new IllegalArgumentException("Valid product is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("Valid user is required");
        }

        final Product product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Users cannot buy their own products");
        }

        final User seller = userService.findById(product.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        final User buyer = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found"));

        if (!productService.decrementStock(productId)) {
            throw new IllegalStateException("Product is no longer available");
        }

        final String buyerToken = UUID.randomUUID().toString();
        final String sellerToken = UUID.randomUUID().toString();

        final LocalDateTime reservedUntil = LocalDateTime.now().plusMinutes(RESERVATION_MINUTES);

        final Purchase purchase;
        try {
            purchase = purchaseDao.createPurchase(productId, userId, seller.getId(), PurchaseStatus.PENDING, buyerToken,
                    sellerToken, reservedUntil);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Product is no longer available", e);
        }

        runAfterCommit(() -> {
            final Locale locale = LocaleContextHolder.getLocale();
            emailService.sendBuyerEmail(
                    buyer.getEmail(),
                    purchase,
                    product,
                    messageSource.getMessage("Email.purchase.buyer.confirmed.title", null, locale),
                    messageSource.getMessage("Email.purchase.buyer.confirmed.msg", null, locale),
                    buyer,
                    seller,
                    PurchaseStatus.PENDING);
            emailService.sendSellerEmail(
                    seller.getEmail(),
                    purchase,
                    product,
                    messageSource.getMessage("Email.purchase.seller.requested.title", null, locale),
                    messageSource.getMessage("Email.purchase.seller.requested.msg",
                            new Object[] { buyer.getUsername() }, locale),
                    buyer,
                    seller,
                    PurchaseStatus.PENDING);
        });

        return purchase;
    }

    private static void runAfterCommit(final Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    @Override
    @Transactional
    public Optional<Purchase> findById(Long purchaseId) {
        final Optional<Purchase> purchase = purchaseDao.findById(purchaseId);
        purchase.ifPresent(value -> cancelIfExpired(value, LocalDateTime.now()));
        return purchase;
    }

    @Override
    @Transactional
    public Purchase updateStatus(
            Long purchaseId,
            Long userId,
            PurchaseStatus newStatus,
            byte[] paymentProof,
            String paymentProofContentType,
            String paymentProofFileName) {
        final Purchase purchase = purchaseDao.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new IllegalStateException("Purchase already cancelled");
        }

        if (cancelIfExpired(purchase, LocalDateTime.now())) {
            throw new PurchaseExpiredException("Purchase reservation expired");
        }

        final Product product = productService.findById(purchase.getProductId())
                .orElseThrow(() -> new IllegalStateException("Product missing"));

        final User seller = userService.findById(product.getUserId())
                .orElseThrow(() -> new IllegalStateException("Seller missing"));

        final User buyer = userService.findById(purchase.getBuyerId())
                .orElseThrow(() -> new IllegalStateException("Buyer missing"));

        final boolean isBuyer = userId.equals(purchase.getBuyerId());
        final boolean isSeller = userId.equals(purchase.getSellerId());

        if (!isBuyer && !isSeller) {
            throw new IllegalArgumentException("You are not authorized to update this purchase");
        }

        // State Machine Validations
        if (newStatus == PurchaseStatus.PAID && isBuyer && purchase.getStatus() == PurchaseStatus.PENDING) {
            if (paymentProof == null || paymentProof.length == 0 || paymentProofContentType == null
                    || paymentProofContentType.trim().isEmpty()) {
                throw new IllegalArgumentException("Payment proof is required");
            }
            purchase.setPaymentProof(paymentProof, paymentProofContentType, paymentProofFileName);
            purchaseDao.updateStatus(purchaseId, newStatus);
            // Notify seller to confirm payment
            final Locale locale = LocaleContextHolder.getLocale();
            emailService.sendSellerEmail(
                    seller.getEmail(),
                    purchase,
                    product,
                    messageSource.getMessage("Email.purchase.seller.paid.title", null, locale),
                    messageSource.getMessage("Email.purchase.seller.paid.msg", new Object[] { buyer.getUsername() },
                            locale),
                    buyer,
                    seller,
                    PurchaseStatus.PAID);
        } else if (newStatus == PurchaseStatus.SHIPPED && isSeller && purchase.getStatus() == PurchaseStatus.PAID) {
            purchaseDao.updateStatus(purchaseId, newStatus);
            // Notify buyer
            final Locale locale = LocaleContextHolder.getLocale();
            emailService.sendBuyerEmail(
                    buyer.getEmail(),
                    purchase,
                    product,
                    messageSource.getMessage("Email.purchase.buyer.shipped.title", null, locale),
                    messageSource.getMessage("Email.purchase.buyer.shipped.msg", null, locale),
                    buyer,
                    seller,
                    PurchaseStatus.SHIPPED);
        } else if (newStatus == PurchaseStatus.DELIVERED && isBuyer && purchase.getStatus() == PurchaseStatus.SHIPPED) {
            purchaseDao.updateStatus(purchaseId, newStatus);
            // Stock was already decremented at purchase creation; no further action needed.
        } else {
            throw new IllegalStateException("Invalid state transition or unauthorized role.");
        }

        return purchaseDao.findById(purchaseId).get();
    }

    @Override
    @Transactional
    public int cancelExpiredPurchases() {
        final LocalDateTime now = LocalDateTime.now();
        final List<Purchase> expired = purchaseDao.findExpiredPending(now);
        int cancelled = 0;
        for (Purchase purchase : expired) {
            if (cancelIfExpired(purchase, now)) {
                cancelled++;
            }
        }
        return cancelled;
    }

    private boolean cancelIfExpired(final Purchase purchase, final LocalDateTime now) {
        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            return false;
        }
        final LocalDateTime reservedUntil = purchase.getReservedUntil();
        if (reservedUntil == null || !reservedUntil.isBefore(now)) {
            return false;
        }
        purchase.setStatus(PurchaseStatus.CANCELLED);
        productService.incrementStock(purchase.getProductId());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Purchase> findByBuyerId(Long buyerId, List<PurchaseStatus> statuses, int page, int pageSize) {
        return purchaseDao.findByBuyerId(buyerId, statuses, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Purchase> findBySellerId(Long sellerId, List<PurchaseStatus> statuses, int page, int pageSize) {
        return purchaseDao.findBySellerId(sellerId, statuses, page, pageSize);
    }
}
