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

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Autowired
    public EmailServiceImpl(final JavaMailSender javaMailSender, final SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    @Override
    public void sendOrderConfirmation(String to, String username, String productName, String orderId) {
        final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("username", username);
        ctx.setVariable("amount", "$19.99"); // Mock data
        ctx.setVariable("productName", productName);
        ctx.setVariable("orderId", orderId);
        ctx.setVariable("redirectUrl", "http://localhost:8000/vinyland/orders/" + orderId);

        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

            messageHelper.setSubject("Vinyland - Order Confirmation #" + orderId); // Can also be resolved via message source
            messageHelper.setTo(to);
            messageHelper.setFrom("no-reply@vinyland.com");

            // Process the Thymeleaf template
            final String htmlContent = templateEngine.process("order-confirmation", ctx);
            messageHelper.setText(htmlContent, true);

            // Send email
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            // Handle error without throwing it if async, or log it
            e.printStackTrace();
        }
    }
}
