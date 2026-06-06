package ar.edu.itba.paw.services;

import java.util.Locale;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

public interface EmailService {

    void sendBuyerEmail(
            Purchase purchase,
            Product product,
            User buyer,
            User seller,
            PurchaseStatus status,
            Locale locale);

    void sendSellerEmail(
            Purchase purchase,
            Product product,
            User buyer,
            User seller,
            PurchaseStatus status,
            Locale locale);

    void sendProductReportEmail(Product product, User reporter, final User seller, Locale locale);
    
    void sendNewVinylDigestEmail(String to, String username, java.util.List<Product> products, Locale locale);

    void sendPasswordResetEmail(String to, String resetToken, String username, Locale locale);
    void sendVerificationEmail(String to, String resetToken, String username, Locale locale);
}
