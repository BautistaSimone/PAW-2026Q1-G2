package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

import java.util.UUID;

@RestController
public class TestMailController {

    private final EmailService emailService;

    @Autowired
    public TestMailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Endpoint to trigger a test email.
     * URI: http://localhost:8000/vinyland/test-mail?email=YOUR_EMAIL@HERE.com
     */
    @GetMapping("/test-mail")
    public String testMail(@RequestParam(value = "email", defaultValue = "test@example.com") String email) {
        String testOrderId = UUID.randomUUID().toString().substring(0, 8);
        
        Product mockProduct = new Product(1L, 1L, "Classic Vinyl: The Dark Side of the Moon", "Pink Floyd", "Harvest", "SHVL 804", "UK", java.util.Collections.emptyList(), "Test", java.math.BigDecimal.TEN, java.math.BigDecimal.TEN, "Test", "Test", java.time.LocalDate.now(), java.math.BigDecimal.valueOf(100));
        Purchase mockPurchase = new Purchase(1L, 1L, 1L, java.time.LocalDate.now(), PurchaseStatus.PENDING, UUID.randomUUID().toString(), UUID.randomUUID().toString());

        emailService.sendBuyerEmail(
            email, 
            mockPurchase,
            mockProduct,
            "Correo de prueba — Vinyland",
            "Este es un mensaje de prueba del sistema de notificaciones de compra. Pedido de referencia: " + testOrderId + ".",
            "Amante del vinilo"
        );
        
        return "Mail sent to " + email + " (asynchronously)! " +
               "Check your mail server (e.g. Mailtrap) or Jetty logs for errors. Order ID: " + testOrderId;
    }
}
