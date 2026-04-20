package ar.edu.itba.paw.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

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

    private static final DateTimeFormatter PURCHASE_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
    public void sendBuyerEmail(
            final String to,
            final Purchase purchase,
            final Product product,
            final String title,
            final String message,
            final User buyer,
            final User seller,
            final PurchaseStatus currentStatus) {
        final String tokenUrl = buildAbsoluteUrl("/purchases/" + purchase.getPurchaseId() + "?token=" + purchase.getBuyerToken());
        sendOrderEmail(to, product, purchase, title, message, tokenUrl, buyer, seller, true, currentStatus);
    }

    @Async
    @Override
    public void sendSellerEmail(
            final String to,
            final Purchase purchase,
            final Product product,
            final String title,
            final String message,
            final User buyer,
            final User seller,
            final PurchaseStatus currentStatus) {
        final String tokenUrl = buildAbsoluteUrl("/purchases/" + purchase.getPurchaseId() + "?token=" + purchase.getSellerToken());
        sendOrderEmail(to, product, purchase, title, message, tokenUrl, buyer, seller, false, currentStatus);
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
        ctx.setVariable("location", safeProductLocation(product));
        ctx.setVariable("recordLabel", nullToEmpty(product.getRecordLabel()));
        ctx.setVariable("catalogNumber", nullToEmpty(product.getCatalogNumber()));
        ctx.setVariable("editionCountry", nullToEmpty(product.getEditionCountry()));
        ctx.setVariable("descriptionExcerpt", excerpt(product.getDescription(), 220));
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

    private void sendOrderEmail(
            final String to,
            final Product product,
            final Purchase purchase,
            final String title,
            final String message,
            final String actionUrl,
            final User buyer,
            final User seller,
            final boolean recipientIsBuyer,
            final PurchaseStatus currentStatus) {
        final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("title", title);
        ctx.setVariable("message", message);
        ctx.setVariable("amount", "$" + product.getPrice());
        ctx.setVariable("productName", product.getTitle() + " - " + product.getArtist());
        ctx.setVariable("actionUrl", actionUrl);
        final String recipientName = recipientIsBuyer
                ? (buyer.getFullName().isEmpty() ? buyer.getUsername() : buyer.getFullName())
                : (seller.getFullName().isEmpty() ? seller.getUsername() : seller.getFullName());
        ctx.setVariable("recipientName", recipientName);
        ctx.setVariable("purchaseId", purchase.getPurchaseId());
        ctx.setVariable("currentStep", currentStatus.ordinal());
        ctx.setVariable("purchaseStatusKey", currentStatus.name());
        ctx.setVariable("purchaseStatusDescription", currentStatus.getDescription());
        ctx.setVariable("formattedPurchaseDate", purchase.getDate() != null
                ? purchase.getDate().format(PURCHASE_DATE_FMT) : "");

        ctx.setVariable("buyer", buyer);
        ctx.setVariable("seller", seller);
        ctx.setVariable("recipientIsBuyer", recipientIsBuyer);
        ctx.setVariable("sellerUsername", seller.getUsername());
        ctx.setVariable("buyerUsername", buyer.getUsername());
        ctx.setVariable("sellerEmail", seller.getEmail());
        ctx.setVariable("sellerCbuCvu", seller.getCbuCvu());
        ctx.setVariable("buyerEmail", buyer.getEmail());
        ctx.setVariable("buyerFullName", buyer.getFullName());
        ctx.setVariable("buyerShippingAddress", buyer.getFormattedShippingAddress());

        ctx.setVariable("productUrl", buildProductUrl(product.getId()));
        ctx.setVariable("recordLabel", nullToEmpty(product.getRecordLabel()));
        ctx.setVariable("catalogNumber", nullToEmpty(product.getCatalogNumber()));
        ctx.setVariable("editionCountry", nullToEmpty(product.getEditionCountry()));
        ctx.setVariable("productLocation", safeProductLocation(product));
        ctx.setVariable("sleeveCondition", product.getSleeveCondition() != null ? product.getSleeveCondition().toPlainString() : "");
        ctx.setVariable("recordCondition", product.getRecordCondition() != null ? product.getRecordCondition().toPlainString() : "");

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

    private static String nullToEmpty(final String s) {
        return s == null ? "" : s.trim();
    }

    private static String excerpt(final String text, final int maxLen) {
        if (text == null || text.isBlank()) {
            return "";
        }
        final String t = text.trim().replaceAll("\\s+", " ");
        return t.length() <= maxLen ? t : t.substring(0, maxLen).trim() + "…";
    }

    private static String safeProductLocation(final Product product) {
        final String n = product.getNeighborhood();
        final String p = product.getProvince();
        final boolean hn = n != null && !n.isBlank();
        final boolean hp = p != null && !p.isBlank();
        if (hn && hp) {
            return n.trim() + ", " + p.trim();
        }
        if (hn) {
            return n.trim();
        }
        if (hp) {
            return p.trim();
        }
        return "";
    }
}
