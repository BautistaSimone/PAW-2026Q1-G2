package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

public interface EmailService {

    void sendBuyerEmail(String to, Purchase purchase, Product product, String title, String message, String recipientName, PurchaseStatus currentStatus);

    void sendSellerEmail(String to, Purchase purchase, Product product, String title, String message, String recipientName, PurchaseStatus currentStatus);

    void sendProductReportEmail(Product product, User reporter);

}
