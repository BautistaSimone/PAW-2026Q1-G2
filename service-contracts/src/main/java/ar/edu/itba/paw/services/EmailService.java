package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.Product;

public interface EmailService {

    void sendBuyerEmail(String to, Purchase purchase, Product product, String title, String message);

    void sendSellerEmail(String to, Purchase purchase, Product product, String title, String message);

}
