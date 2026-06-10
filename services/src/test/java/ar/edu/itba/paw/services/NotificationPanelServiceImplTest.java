package ar.edu.itba.paw.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

@ExtendWith(MockitoExtension.class)
public class NotificationPanelServiceImplTest {

    private static final long USER_ID = 1L;
    private static final long ACTOR_ID = 2L;
    private static final long PRODUCT_ID = 10L;
    private static final int PAGE = 1;
    private static final int PAGE_SIZE = 8;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    private NotificationPanelServiceImpl notificationPanelService;

    @BeforeEach
    public void setUp() {
        notificationPanelService = new NotificationPanelServiceImpl(
                notificationService,
                userService,
                productService);
    }

    @Test
    public void getPanelDataReturnsDataWithNotifications() {
        final Notification notification = new Notification(
                USER_ID, ACTOR_ID, NotificationType.FOLLOW, PRODUCT_ID, null, null);
        final List<Notification> notifications = List.of(notification);
        final PaginatedResult<Notification> page = new PaginatedResult<>(notifications, PAGE, PAGE_SIZE, 1);

        final User actor = new User(ACTOR_ID, "actor@test.com", "pass", "actor", false, true, false, null, null, null, null, null, null, null, null);
        final Product product = new Product(PRODUCT_ID, 3L, "Album", "Artist", "Label", "CAT", "Country", Collections.emptyList(), "Desc", null, null, null, null, 1);

        Mockito.when(notificationService.listForUser(USER_ID, NotificationType.FOLLOW, PAGE, PAGE_SIZE)).thenReturn(page);
        Mockito.when(userService.findByIds(List.of(ACTOR_ID))).thenReturn(List.of(actor));
        Mockito.when(productService.findByIds(java.util.Set.of(PRODUCT_ID))).thenReturn(List.of(product));
        Mockito.when(notificationService.countUnread(USER_ID)).thenReturn(3L);

        final NotificationPanelService.PanelData result = notificationPanelService.getPanelData(
                USER_ID, NotificationType.FOLLOW, PAGE, PAGE_SIZE);

        Assertions.assertSame(page, result.getPage());
        Assertions.assertEquals(notifications, result.getNotifications());
        Assertions.assertEquals(1, result.getUsersById().size());
        Assertions.assertSame(actor, result.getUsersById().get(ACTOR_ID));
        Assertions.assertEquals(1, result.getProductsById().size());
        Assertions.assertSame(product, result.getProductsById().get(PRODUCT_ID));
        Assertions.assertEquals("FOLLOW", result.getFilter());
        Assertions.assertEquals(3L, result.getUnreadCount());
    }

    @Test
    public void getPanelDataReturnsEmptyMapsWhenNoNotifications() {
        final PaginatedResult<Notification> emptyPage = new PaginatedResult<>(
                Collections.emptyList(), PAGE, PAGE_SIZE, 0);

        Mockito.when(notificationService.listForUser(USER_ID, null, PAGE, PAGE_SIZE)).thenReturn(emptyPage);
        Mockito.when(notificationService.countUnread(USER_ID)).thenReturn(0L);

        final NotificationPanelService.PanelData result = notificationPanelService.getPanelData(
                USER_ID, null, PAGE, PAGE_SIZE);

        Assertions.assertTrue(result.getNotifications().isEmpty());
        Assertions.assertTrue(result.getUsersById().isEmpty());
        Assertions.assertTrue(result.getProductsById().isEmpty());
        Assertions.assertEquals("ALL", result.getFilter());
        Assertions.assertEquals(0L, result.getUnreadCount());
    }

    @Test
    public void getPanelDataSkipsNullActorIds() {
        final Notification notification = new Notification(
                USER_ID, null, NotificationType.NEW_PRODUCT, PRODUCT_ID, null, null);
        final PaginatedResult<Notification> page = new PaginatedResult<>(
                List.of(notification), PAGE, PAGE_SIZE, 1);
        final Product product = new Product(PRODUCT_ID, 3L, "Album", "Artist", "Label", "CAT", "Country", Collections.emptyList(), "Desc", null, null, null, null, 1);

        Mockito.when(notificationService.listForUser(USER_ID, NotificationType.NEW_PRODUCT, PAGE, PAGE_SIZE)).thenReturn(page);
        Mockito.when(productService.findByIds(java.util.Set.of(PRODUCT_ID))).thenReturn(List.of(product));
        Mockito.when(notificationService.countUnread(USER_ID)).thenReturn(0L);

        final NotificationPanelService.PanelData result = notificationPanelService.getPanelData(
                USER_ID, NotificationType.NEW_PRODUCT, PAGE, PAGE_SIZE);

        Assertions.assertTrue(result.getUsersById().isEmpty());
        Assertions.assertEquals(1, result.getProductsById().size());
    }

    @Test
    public void getPanelDataSkipsNullProductIds() {
        final Notification notification = new Notification(
                USER_ID, ACTOR_ID, NotificationType.REVIEW_RECEIVED, null, null, null);
        final PaginatedResult<Notification> page = new PaginatedResult<>(
                List.of(notification), PAGE, PAGE_SIZE, 1);
        final User actor = new User(ACTOR_ID, "actor@test.com", "pass", "actor", false, true, false, null, null, null, null, null, null, null, null);

        Mockito.when(notificationService.listForUser(USER_ID, NotificationType.REVIEW_RECEIVED, PAGE, PAGE_SIZE)).thenReturn(page);
        Mockito.when(userService.findByIds(List.of(ACTOR_ID))).thenReturn(List.of(actor));
        Mockito.when(notificationService.countUnread(USER_ID)).thenReturn(0L);

        final NotificationPanelService.PanelData result = notificationPanelService.getPanelData(
                USER_ID, NotificationType.REVIEW_RECEIVED, PAGE, PAGE_SIZE);

        Assertions.assertEquals(1, result.getUsersById().size());
        Assertions.assertTrue(result.getProductsById().isEmpty());
    }
}