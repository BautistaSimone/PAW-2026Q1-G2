package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.User;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

        private EmailServiceImpl emailService;

        @Mock
        private JavaMailSender javaMailSender;

        @Mock
        private SpringTemplateEngine templateEngine;

        @Mock
        private MessageSource messageSource;

        @Mock
        private MimeMessage mimeMessage;

        private User buyer;
        private User seller;
        private Product product;
        private Purchase purchase;

        @BeforeEach
        public void setUp() {
                emailService = new EmailServiceImpl(javaMailSender, templateEngine, messageSource, "http://localhost/",
                                "admin@vinyland.com");

                buyer = new User(1L, "buyer@test.com", "pass", "buyer", false, true, false, "BuyerName",
                                "BuyerLastName", "Street", "123", "Neighborhood", "Province", "Extra",
                                "1234567890123456789012");
                seller = new User(2L, "seller@test.com", "pass", "seller", false, true, false, "SellerName",
                                "SellerLastName", "Street2", "456", "Neighborhood2", "Province2", "Extra2",
                                "2234567890123456789012");
                product = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country",
                                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(),
                                BigDecimal.valueOf(100), 1);
                purchase = new Purchase(100L, 10L, 1L, 2L, LocalDate.now(), PurchaseStatus.PENDING, "bToken", "sToken");
        }

        @Test
        public void testSendBuyerEmail() {
                // Arrange
                Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any(Locale.class)))
                                .thenReturn("LocalizedText");
                Mockito.when(templateEngine.process(Mockito.eq("order-notification"), Mockito.any(Context.class)))
                                .thenReturn("<html>Buyer HTML</html>");

                // Act
                emailService.sendBuyerEmail(purchase, product, buyer, seller,
                                PurchaseStatus.PENDING, Locale.ENGLISH);

                // Assert
                Mockito.verify(javaMailSender, Mockito.times(1)).send(mimeMessage);
        }

        @Test
        public void testSendSellerEmail() {
                // Arrange
                Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any(Locale.class)))
                                .thenReturn("LocalizedText");
                Mockito.when(templateEngine.process(Mockito.eq("order-notification"), Mockito.any(Context.class)))
                                .thenReturn("<html>Seller HTML</html>");

                // Act
                emailService.sendSellerEmail(purchase, product, buyer,
                                seller, PurchaseStatus.PENDING, Locale.ENGLISH);

                // Assert
                Mockito.verify(javaMailSender, Mockito.times(1)).send(mimeMessage);
        }

        @Test
        public void testSendProductReportEmail() {
                // Arrange
                Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any(Locale.class)))
                                .thenReturn("LocalizedText");
                Mockito.when(templateEngine.process(Mockito.eq("product-report-notification"),
                                Mockito.any(Context.class)))
                                .thenReturn("<html>Report HTML</html>");

                User reporter = new User(3L, "rep@test.com", "pass", "reporter", false, true, false, "Rep", "Last",
                                null, null, null, null, null, null);

                // Act
                emailService.sendProductReportEmail(product, reporter, seller, Locale.ENGLISH);

                // Assert
                Mockito.verify(javaMailSender, Mockito.times(1)).send(mimeMessage);
        }

        @Test
        public void testSendPasswordResetEmail() {
                // Arrange
                Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any(Locale.class)))
                                .thenReturn("LocalizedText");
                Mockito.when(templateEngine.process(Mockito.eq("password-reset"), Mockito.any(Context.class)))
                                .thenReturn("<html>Reset HTML</html>");

                // Act
                emailService.sendPasswordResetEmail("user@test.com", "token", "username", Locale.ENGLISH);

                // Assert
                Mockito.verify(javaMailSender, Mockito.times(1)).send(mimeMessage);
        }

        @Test
        public void testSendVerificationEmail() {
                // Arrange
                Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any(Locale.class)))
                                .thenReturn("LocalizedText");
                Mockito.when(templateEngine.process(Mockito.eq("verification"), Mockito.any(Context.class)))
                                .thenReturn("<html>Verify HTML</html>");

                // Act
                emailService.sendVerificationEmail("user@test.com", "token", "username", Locale.ENGLISH);

                // Assert
                Mockito.verify(javaMailSender, Mockito.times(1)).send(mimeMessage);
        }

        @Test
        public void testSendNewVinylDigestEmail() {
                // Arrange
                Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any(Locale.class)))
                                .thenReturn("LocalizedText");
                Mockito.when(templateEngine.process(Mockito.eq("new-vinyl-digest"), Mockito.any(Context.class)))
                                .thenReturn("<html>Digest HTML</html>");

                product.setUserId(2L);
                try {
                        java.lang.reflect.Field sellerField = Product.class.getDeclaredField("seller");
                        sellerField.setAccessible(true);
                        sellerField.set(product, seller);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

                // Act
                emailService.sendNewVinylDigestEmail("recipient@test.com", "recipient", Arrays.asList(product),
                                Locale.ENGLISH);

                // Assert
                Mockito.verify(javaMailSender, Mockito.times(1)).send(mimeMessage);
        }
}
