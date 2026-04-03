package ar.edu.itba.paw.services;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.Product;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final String baseUrl = "http://localhost:8000";

    @Autowired
    public EmailServiceImpl(final JavaMailSender javaMailSender, final SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    @Override
    public void sendBuyerEmail(String to, Purchase purchase, Product product, String title, String message) {
        String tokenUrl = baseUrl + "/purchases/" + purchase.getPurchaseId() + "?token=" + purchase.getBuyerToken();
        sendEmail(to, product, title, message, tokenUrl);
    }

    @Async
    @Override
    public void sendSellerEmail(String to, Purchase purchase, Product product, String title, String message) {
        String tokenUrl = baseUrl + "/purchases/" + purchase.getPurchaseId() + "?token=" + purchase.getSellerToken();
        sendEmail(to, product, title, message, tokenUrl);
    }

    private void sendEmail(String to, Product product, String title, String message, String actionUrl) {
        final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("title", title);
        ctx.setVariable("message", message);
        ctx.setVariable("amount", "$" + product.getPrice());
        ctx.setVariable("productName", product.getTitle() + " - " + product.getArtist());
        ctx.setVariable("actionUrl", actionUrl);

        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

            messageHelper.setSubject("Vinyland - " + title);
            messageHelper.setTo(to);
            messageHelper.setFrom("no-reply@vinyland.com");

            final String htmlContent = templateEngine.process("order-notification", ctx);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            System.out.println("Email effectively sent to: " + to + " | Action URL: " + actionUrl);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
