package ar.edu.itba.paw.services;

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

        if (!productService.reserveIfAvailable(productId)) {
            throw new IllegalStateException("Product is no longer available");
        }

        final String buyerToken = UUID.randomUUID().toString();
        final String sellerToken = UUID.randomUUID().toString();

        final Purchase purchase;
        try {
            purchase = purchaseDao.createPurchase(productId, userId, seller.getId(), PurchaseStatus.PENDING, buyerToken,
                    sellerToken);
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
    @Transactional(readOnly = true)
    public Optional<Purchase> findById(Long purchaseId) {
        return purchaseDao.findById(purchaseId);
    }

    @Override
    @Transactional
    public Purchase updateStatus(Long purchaseId, Long userId, PurchaseStatus newStatus) {
        final Purchase purchase = purchaseDao.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

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
            productService.markAsSold(purchase.getProductId());
        } else {
            throw new IllegalStateException("Invalid state transition or unauthorized role.");
        }

        return purchaseDao.findById(purchaseId).get();
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
