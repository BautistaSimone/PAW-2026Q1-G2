package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        
        emailService.sendOrderConfirmation(
            email, 
            "Test Enthusiast", 
            "Classic Vinyl: The Dark Side of the Moon", 
            testOrderId
        );
        
        return "Mail sent to " + email + " (asynchronously)! " +
               "Check your mail server (e.g. Mailtrap) or Jetty logs for errors. Order ID: " + testOrderId;
    }
}
