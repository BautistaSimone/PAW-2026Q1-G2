package ar.edu.itba.paw.services;

public interface EmailService {

    /**
     * Sends an order confirmation email asynchronously to the user.
     *
     * @param to          The recipient's email address
     * @param username    The name of the user receiving the email
     * @param productName The name of the product purchased
     * @param orderId     The order identifier for reference
     */
    void sendOrderConfirmation(String to, String username, String productName, String orderId);

}
