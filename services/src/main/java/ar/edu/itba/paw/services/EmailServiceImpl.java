package ar.edu.itba.paw.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final ProductReportRemovalTokenService reportRemovalTokenService;
    private final String baseUrl;
    private final String adminEmail;

    @Autowired
    public EmailServiceImpl(
        final JavaMailSender javaMailSender,
        final SpringTemplateEngine templateEngine,
        final ProductReportRemovalTokenService reportRemovalTokenService,
        @Value("${app.base.url:http://localhost:8000}") final String baseUrl,
        @Value("${mail.username}") final String adminEmail
    ) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.reportRemovalTokenService = reportRemovalTokenService;
        this.baseUrl = baseUrl;
        this.adminEmail = adminEmail;
    }

    @Async
    @Override
    public void sendBuyerEmail(String to, Purchase purchase, Product product, String title, String message, String recipientName, PurchaseStatus currentStatus) {
        String tokenUrl = buildAbsoluteUrl("/purchases/" + purchase.getPurchaseId() + "?token=" + purchase.getBuyerToken());
        sendEmail(to, product, purchase.getPurchaseId(), title, message, tokenUrl, recipientName, currentStatus);
    }

    @Async
    @Override
    public void sendSellerEmail(String to, Purchase purchase, Product product, String title, String message, String recipientName, PurchaseStatus currentStatus) {
        String tokenUrl = buildAbsoluteUrl("/purchases/" + purchase.getPurchaseId() + "?token=" + purchase.getSellerToken());
        sendEmail(to, product, purchase.getPurchaseId(), title, message, tokenUrl, recipientName, currentStatus);
    }

    @Async
    @Override
    public void sendProductReportEmail(final Product product, final User reporter) {
        final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("title", "Nueva publicación reportada");
        ctx.setVariable("message", "Se reportó una publicación y requiere revisión manual por parte del equipo de moderación.");
        ctx.setVariable("productId", product.getId());
        ctx.setVariable("productName", product.getTitle() + " - " + product.getArtist());
        ctx.setVariable("amount", "$" + product.getPrice());
        ctx.setVariable("location", product.getLocation());
        ctx.setVariable("reporterName", reporter.getUsername());
        ctx.setVariable("reporterEmail", reporter.getEmail());
        final String removalToken = reportRemovalTokenService.createToken(product.getId());
        final String encodedToken = URLEncoder.encode(removalToken, StandardCharsets.UTF_8);
        ctx.setVariable(
            "removeActionUrl",
            buildAbsoluteUrl("/products/" + product.getId() + "/moderate-hide?token=" + encodedToken)
        );
        ctx.setVariable("viewProductUrl", buildProductUrl(product.getId()));

        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

            messageHelper.setSubject("Vinyland - Publicación reportada");
            messageHelper.setTo(adminEmail);
            messageHelper.setFrom("no-reply@vinyland.com");

            final String htmlContent = templateEngine.process("product-report-notification", ctx);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            System.out.println("Product report email sent for product: " + product.getId());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(String to, Product product, long purchaseId, String title, String message, String actionUrl, String recipientName, PurchaseStatus currentStatus) {
        final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("title", title);
        ctx.setVariable("message", message);
        ctx.setVariable("amount", "$" + product.getPrice());
        ctx.setVariable("productName", product.getTitle() + " - " + product.getArtist());
        ctx.setVariable("actionUrl", actionUrl);
        ctx.setVariable("recipientName", recipientName);
        ctx.setVariable("purchaseId", purchaseId);
        ctx.setVariable("currentStep", currentStatus.ordinal());

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

    private String buildProductUrl(final Long productId) {
        return buildAbsoluteUrl("/products/" + productId);
    }

    private String buildAbsoluteUrl(final String path) {
        final String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + path;
    }
}
