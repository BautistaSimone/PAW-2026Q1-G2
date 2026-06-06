package ar.edu.itba.paw.services;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import ar.edu.itba.paw.models.PendingNotification;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductState;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.PendingNotificationDao;
import ar.edu.itba.paw.persistence.ProductDao;

@ExtendWith(MockitoExtension.class)
public class PendingNotificationServiceImplTest {

    @InjectMocks
    private PendingNotificationServiceImpl pendingNotificationService;

    @Mock
    private PendingNotificationDao pendingNotificationDao;

    @Mock
    private ProductDao productDao;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    private PendingNotification createPendingNotification(Long notificationId, Long followerUserId, Long productId)
            throws Exception {
        PendingNotification pn = new PendingNotification(followerUserId, productId);
        Field field = PendingNotification.class.getDeclaredField("notificationId");
        field.setAccessible(true);
        field.set(pn, notificationId);
        return pn;
    }

    @Test
    public void testEnqueueForFollowers() {
        // Act
        pendingNotificationService.enqueueForFollowers(1L, 10L);

        // Assert
        Mockito.verify(pendingNotificationDao, Mockito.times(1))
                .createForAllFollowersOf(1L, 10L);
    }

    @Test
    public void testProcessAndSendDigestEmailsEmpty() {
        // Arrange
        Mockito.when(pendingNotificationDao.findAll()).thenReturn(Collections.emptyList());

        // Act
        pendingNotificationService.processAndSendDigestEmails();

        // Assert
        Mockito.verifyNoInteractions(productDao, userService, emailService);
        Mockito.verify(pendingNotificationDao, Mockito.never()).deleteByIds(Mockito.anyList());
    }

    @Test
    public void testProcessAndSendDigestEmailsUserNotFound() throws Exception {
        // Arrange
        PendingNotification pn = createPendingNotification(100L, 1L, 10L);
        Mockito.when(pendingNotificationDao.findAll()).thenReturn(Collections.singletonList(pn));
        Mockito.when(productDao.findByIds(Collections.singleton(10L))).thenReturn(Collections.emptyList());
        Mockito.when(userService.findById(1L)).thenReturn(Optional.empty());

        // Act
        pendingNotificationService.processAndSendDigestEmails();

        // Assert
        Mockito.verifyNoInteractions(emailService);
        Mockito.verify(pendingNotificationDao, Mockito.times(1)).deleteByIds(Collections.singletonList(100L));
    }

    @Test
    public void testProcessAndSendDigestEmailsProductNotFoundOrInactive() throws Exception {
        // Arrange
        PendingNotification pn = createPendingNotification(100L, 1L, 10L);
        Mockito.when(pendingNotificationDao.findAll()).thenReturn(Collections.singletonList(pn));
        Mockito.when(productDao.findByIds(Collections.singleton(10L))).thenReturn(Collections.emptyList()); // product
                                                                                                            // not found

        User user = new User(1L, "user@test.com", "pass", "user", false, true, false, null, null, null, null, null,
                null, null, null);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(user));

        // Act
        pendingNotificationService.processAndSendDigestEmails();

        // Assert
        Mockito.verifyNoInteractions(emailService);
        Mockito.verify(pendingNotificationDao, Mockito.times(1)).deleteByIds(Collections.singletonList(100L));
    }

    @Test
    public void testProcessAndSendDigestEmailsProductInactive() throws Exception {
        // Arrange
        PendingNotification pn = createPendingNotification(100L, 1L, 10L);
        Mockito.when(pendingNotificationDao.findAll()).thenReturn(Collections.singletonList(pn));

        Product inactiveProduct = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(),
                BigDecimal.valueOf(100), 1);
        inactiveProduct.setState(ProductState.SOLD); // inactive
        Mockito.when(productDao.findByIds(Collections.singleton(10L)))
                .thenReturn(Collections.singletonList(inactiveProduct));

        User user = new User(1L, "user@test.com", "pass", "user", false, true, false, null, null, null, null, null,
                null, null, null);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(user));

        // Act
        pendingNotificationService.processAndSendDigestEmails();

        // Assert
        Mockito.verifyNoInteractions(emailService);
        Mockito.verify(pendingNotificationDao, Mockito.times(1)).deleteByIds(Collections.singletonList(100L));
    }

    @Test
    public void testProcessAndSendDigestEmailsSuccess() throws Exception {
        // Arrange
        PendingNotification pn = createPendingNotification(100L, 1L, 10L);
        Mockito.when(pendingNotificationDao.findAll()).thenReturn(Collections.singletonList(pn));

        Product activeProduct = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(),
                BigDecimal.valueOf(100), 1);
        activeProduct.setState(ProductState.ACTIVE); // active
        Mockito.when(productDao.findByIds(Collections.singleton(10L)))
                .thenReturn(Collections.singletonList(activeProduct));

        User user = new User(1L, "user@test.com", "pass", "user", false, true, false, null, null, null, null, null,
                null, null, null);
        user.setLanguage("en");
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(user));

        // Act
        pendingNotificationService.processAndSendDigestEmails();

        // Assert
        Mockito.verify(emailService, Mockito.times(1))
                .sendNewVinylDigestEmail("user@test.com", "user", Collections.singletonList(activeProduct),
                        Locale.ENGLISH);
        Mockito.verify(pendingNotificationDao, Mockito.times(1)).deleteByIds(Collections.singletonList(100L));
    }

    @Test
    public void testProcessAndSendDigestEmailsExceptionOnMail() throws Exception {
        // Arrange
        PendingNotification pn = createPendingNotification(100L, 1L, 10L);
        Mockito.when(pendingNotificationDao.findAll()).thenReturn(Collections.singletonList(pn));

        Product activeProduct = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(),
                BigDecimal.valueOf(100), 1);
        activeProduct.setState(ProductState.ACTIVE);
        Mockito.when(productDao.findByIds(Collections.singleton(10L)))
                .thenReturn(Collections.singletonList(activeProduct));

        User user = new User(1L, "user@test.com", "pass", "user", false, true, false, null, null, null, null, null,
                null, null, null);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(user));

        Mockito.doThrow(new RuntimeException("Mail server down"))
                .when(emailService).sendNewVinylDigestEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),
                        Mockito.any(Locale.class));

        // Act
        pendingNotificationService.processAndSendDigestEmails();

        // Assert — should catch exception, log it, and still delete processed IDs
        Mockito.verify(pendingNotificationDao, Mockito.times(1)).deleteByIds(Collections.singletonList(100L));
    }
}
