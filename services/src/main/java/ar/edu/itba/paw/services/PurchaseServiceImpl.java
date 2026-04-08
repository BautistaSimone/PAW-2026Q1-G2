package ar.edu.itba.paw.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Purchase createPurchase(Long productId, String buyerEmail) {
        final Product product = productService.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        final User seller = userService.findById(product.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        final User buyer = userService.findByEmail(buyerEmail).orElseGet(() ->
            userService.createUser(buyerEmail, "password", buyerEmail.split("@")[0], false)
        );

        String buyerToken = UUID.randomUUID().toString();
        String sellerToken = UUID.randomUUID().toString();

        Purchase purchase = purchaseDao.createPurchase(productId, buyer.getId(), seller.getId(), PurchaseStatus.PENDING, buyerToken, sellerToken);

        // Notify Buyer
        emailService.sendBuyerEmail(
            buyer.getEmail(), 
            purchase, 
            product, 
            "Comunicate con " + seller.getEmail() + " para abonar tu vinilo", 
            "Has iniciado la compra de este rematado vinilo. Una vez abonado, entra al enlace debajo para notificar al vendedor que ya pagaste.",
            buyer.getUsername()
        );

        return purchase;
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

        boolean isBuyer = token.equals(purchase.getBuyerToken());
        boolean isSeller = token.equals(purchase.getSellerToken());

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
                seller.getUsername()
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
                buyer.getUsername()
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
}
