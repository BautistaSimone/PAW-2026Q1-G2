package ar.edu.itba.paw.services;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final DateTimeFormatter PURCHASE_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PRICE_LOCALE = Locale.forLanguageTag("es-AR");

    private final MessageSource messageSource;

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final String baseUrl;
    private final String adminEmail;

    @Autowired
    public EmailServiceImpl(
        final JavaMailSender javaMailSender,
        final SpringTemplateEngine templateEngine,
        final MessageSource messageSource,
        @Value("${app.base.url:http://pawserver.it.itba.edu.ar/paw-2026a-02/}") final String baseUrl,
        @Value("${mail.username}") final String adminEmail) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
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
    public void sendProductReportEmail(final Product product, final User reporter, final User seller) {

        final Locale locale = LocaleContextHolder.getLocale();

        final Context ctx = new Context(LocaleContextHolder.getLocale());
        
        ctx.setVariable("title",
                messageSource.getMessage("email.report.heading", null, locale));

        ctx.setVariable("message",
                messageSource.getMessage("email.report.message", null, locale));
        ctx.setVariable("productId", product.getId());
        ctx.setVariable("productName", product.getTitle() + " - " + product.getArtist());
        ctx.setVariable("amount", formatAmount(product));
        ctx.setVariable("location", safeProductLocation(seller));
        ctx.setVariable("recordLabel", nullToEmpty(product.getRecordLabel()));
        ctx.setVariable("catalogNumber", nullToEmpty(product.getCatalogNumber()));
        ctx.setVariable("editionCountry", nullToEmpty(product.getEditionCountry()));
        ctx.setVariable("descriptionExcerpt", excerpt(product.getDescription(), 220));
        ctx.setVariable("reporterName", reporter.getUsername());
        ctx.setVariable("reporterEmail", reporter.getEmail());
        ctx.setVariable("viewProductUrl", buildProductUrl(product.getId()));

        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

            messageHelper.setSubject(
                messageSource.getMessage("email.report.title", null, locale)
            );
            messageHelper.setTo(adminEmail);
            messageHelper.setFrom("no-reply@vinyland.com");

            final String htmlContent = templateEngine.process("product-report-notification", ctx);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            LOGGER.info("Product report email sent for product: {}", product.getId());
        } catch (MessagingException e) {
            LOGGER.error("Error sending product report email for product: {}", product.getId(), e);
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String resetToken, String username) {

        final Locale locale = LocaleContextHolder.getLocale();

        String resetUrl = buildAbsoluteUrl("/changePassword?token=" + resetToken);

        final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("title",
            messageSource.getMessage("email.reset.heading", null, locale));
        ctx.setVariable("message",
            messageSource.getMessage("email.reset.instructions", null, locale));
        ctx.setVariable("recipientName", username);
        ctx.setVariable("actionUrl", resetUrl);

        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

            messageHelper.setSubject(
                messageSource.getMessage("email.reset.title", null, locale)
            );
            messageHelper.setTo(to);
            messageHelper.setFrom("no-reply@vinyland.com");

            final String htmlContent = templateEngine.process("password-reset", ctx);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            LOGGER.error("Error sending password reset email to: {}", to, e);
        }
    }

    @Async
    @Override
    public void sendVerificationEmail(String to, String resetToken, String username) {

        final Locale locale = LocaleContextHolder.getLocale();

        String resetUrl = buildAbsoluteUrl("/verifyEmail?token=" + resetToken);

        final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("title",
            messageSource.getMessage("email.verify.heading", null, locale));
        ctx.setVariable("message",
                messageSource.getMessage("email.verify.instructions", null, locale));
        ctx.setVariable("recipientName", username);
        ctx.setVariable("actionUrl", resetUrl);

        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

            messageHelper.setSubject(
                messageSource.getMessage("email.verify.title", null, locale)
            );
            messageHelper.setTo(to);
            messageHelper.setFrom("no-reply@vinyland.com");

            final String htmlContent = templateEngine.process("verification", ctx);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LOGGER.error("Error sending verification email to: {}", to, e);
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
        ctx.setVariable("amount", formatAmount(product));
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
        ctx.setVariable("productLocation", safeProductLocation(seller));
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
            LOGGER.info("Email effectively sent to: {} | Action URL: {}", to, actionUrl);

        } catch (MessagingException e) {
            LOGGER.error("Error sending order email to: {} for purchase: {}", to, purchase.getPurchaseId(), e);
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

    private static String formatAmount(final Product product) {
        final NumberFormat priceFormat = NumberFormat.getNumberInstance(PRICE_LOCALE);
        priceFormat.setGroupingUsed(true);
        priceFormat.setMinimumFractionDigits(0);
        priceFormat.setMaximumFractionDigits(2);
        return "$" + priceFormat.format(product.getPrice());
    }

    private static String safeProductLocation(final User seller) {
        final String n = seller.getNeighborhood();
        final String p = seller.getProvince();
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
