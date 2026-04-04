package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.Product;

public interface EmailService {

    void sendBuyerEmail(String to, Purchase purchase, Product product, String title, String message, String recipientName);

    void sendSellerEmail(String to, Purchase purchase, Product product, String title, String message, String recipientName);

}
