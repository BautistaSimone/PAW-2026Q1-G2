package ar.edu.itba.paw.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

@Service
public class NotificationPanelServiceImpl implements NotificationPanelService {

    private final NotificationService notificationService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public NotificationPanelServiceImpl(
            final NotificationService notificationService,
            final UserService userService,
            final ProductService productService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.productService = productService;
    }

    @Override
    @Transactional(readOnly = true)
    public PanelData getPanelData(final Long userId, final NotificationType filter, final int page, final int pageSize) {
        final PaginatedResult<Notification> notificationsPage =
                notificationService.listForUser(userId, filter, page, pageSize);

        final List<Notification> notifications = notificationsPage.getResults();

        final Map<Long, User> usersById = loadUsersById(notifications);
        final Map<Long, Product> productsById = loadProductsById(notifications);

        final long unreadCount = notificationService.countUnread(userId);
        final String filterLabel = filter == null ? "ALL" : filter.name();

        return new PanelData(notificationsPage, notifications, usersById, productsById, filterLabel, unreadCount);
    }

    private Map<Long, User> loadUsersById(final List<Notification> notifications) {
        final Set<Long> actorIds = notifications.stream()
                .map(Notification::getActorUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (actorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<User> users = userService.findByIds(actorIds.stream().collect(Collectors.toList()));
        final Map<Long, User> result = new HashMap<>();
        for (User user : users) {
            result.put(user.getId(), user);
        }
        return result;
    }

    private Map<Long, Product> loadProductsById(final List<Notification> notifications) {
        final Set<Long> productIds = new HashSet<>();
        for (Notification notification : notifications) {
            if (notification.getProductId() != null) {
                productIds.add(notification.getProductId());
            }
        }

        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<Product> products = productService.findByIds(productIds);
        final Map<Long, Product> result = new HashMap<>();
        for (Product product : products) {
            result.put(product.getId(), product);
        }
        return result;
    }
}