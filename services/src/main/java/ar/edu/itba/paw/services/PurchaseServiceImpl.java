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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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

    @Autowired
    public PurchaseServiceImpl(
            final PurchaseDao purchaseDao, 
            final ProductService productService,
            final UserService userService,
            final EmailService emailService) {
        this.purchaseDao = purchaseDao;
        this.productService = productService;
        this.userService = userService;
        this.emailService = emailService;
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
            purchase = purchaseDao.createPurchase(productId, userId, seller.getId(), PurchaseStatus.PENDING, buyerToken, sellerToken);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Product is no longer available", e);
        }

        runAfterCommit(() ->
            emailService.sendBuyerEmail(
                buyer.getEmail(),
                purchase,
                product,
                "Comunicate con " + seller.getEmail() + " para abonar tu vinilo",
                "Has iniciado la compra de este vinilo. Una vez abonado, entra al enlace debajo para notificar al vendedor que ya pagaste.",
                buyer.getUsername(),
                PurchaseStatus.PENDING
            )
        );

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
    public Optional<Purchase> findById(Long purchaseId) {
        return purchaseDao.findById(purchaseId);
    }

    @Override
    public Purchase updateStatus(Long purchaseId, String token, PurchaseStatus newStatus) {
        final Purchase purchase = purchaseDao.findById(purchaseId)
            .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        final Product product = productService.findById(purchase.getProductId())
            .orElseThrow(() -> new IllegalStateException("Product missing"));

        final User seller = userService.findById(product.getUserId())
            .orElseThrow(() -> new IllegalStateException("Seller missing"));

        final User buyer = userService.findById(purchase.getBuyerId())
            .orElseThrow(() -> new IllegalStateException("Buyer missing"));

        boolean isBuyer = MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), purchase.getBuyerToken().getBytes(StandardCharsets.UTF_8));
        boolean isSeller = MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), purchase.getSellerToken().getBytes(StandardCharsets.UTF_8));

        if (!isBuyer && !isSeller) {
            throw new IllegalArgumentException("Invalid token for this purchase");
        }

        // State Machine Validations
        if (newStatus == PurchaseStatus.PAID && isBuyer && purchase.getStatus() == PurchaseStatus.PENDING) {
            purchaseDao.updateStatus(purchaseId, newStatus);
            // Notify seller to confirm payment
            emailService.sendSellerEmail(
                seller.getEmail(), 
                purchase, 
                product, 
                "El comprador ha pagado", 
                "El comprador " + buyer.getUsername() + " dice haber pagado. Verifica y marca el pago como recibido.",
                seller.getUsername(),
                PurchaseStatus.PAID
            );
        } 
        else if (newStatus == PurchaseStatus.SHIPPED && isSeller && purchase.getStatus() == PurchaseStatus.PAID) {
            purchaseDao.updateStatus(purchaseId, newStatus);
            // Notify buyer
            emailService.sendBuyerEmail(
                buyer.getEmail(), 
                purchase, 
                product, 
                "Tu vinilo ha sido enviado", 
                "El vendedor ha despachado el vinilo. Avisanos cuando te llegue desde el detalle de la compra.",
                buyer.getUsername(),
                PurchaseStatus.SHIPPED
            );
        }
        else if (newStatus == PurchaseStatus.DELIVERED && isBuyer && purchase.getStatus() == PurchaseStatus.SHIPPED) {
            purchaseDao.updateStatus(purchaseId, newStatus);
            productService.markAsSold(purchase.getProductId());
        }
        else {
            throw new IllegalStateException("Invalid state transition or unathorized role.");
        }

        return purchaseDao.findById(purchaseId).get();
    }

    @Override
    public List<Purchase> findByBuyerId(Long buyerId) {
        return purchaseDao.findByBuyerId(buyerId);
    }

    @Override
    public List<Purchase> findBySellerId(Long sellerId) {
        return purchaseDao.findBySellerId(sellerId);
    }
}
