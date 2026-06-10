package ar.edu.itba.paw.webapp.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.NotificationService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;

@ControllerAdvice
public class NotificationPanelAdvice {

    private static final int PANEL_PAGE_SIZE = 8;

    private final NotificationService notificationService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public NotificationPanelAdvice(
            final NotificationService notificationService,
            final UserService userService,
            final ProductService productService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.productService = productService;
    }

    @ModelAttribute
    public void addNotificationPanel(
            final Model model,
            @AuthenticationPrincipal final PawAuthUser authUser,
            final HttpServletRequest request) {
        if (authUser == null || authUser.getUser() == null) {
            return;
        }

        final int page = parsePageParam(request.getParameter("notifPage"));
        final String filterParam = request.getParameter("notifFilter");
        final NotificationType filter = parseFilter(filterParam);
        final String filterLabel = filter == null ? "ALL" : filter.name();

        final Long userId = authUser.getUser().getId();
        final PaginatedResult<Notification> notificationsPage =
                notificationService.listForUser(userId, filter, page, PANEL_PAGE_SIZE);

        final List<Notification> notifications = notificationsPage.getResults();

        final Map<Long, User> usersById = loadUsersById(notifications);
        final Map<Long, Product> productsById = loadProductsById(notifications);

        model.addAttribute("notificationPanelPage", notificationsPage);
        model.addAttribute("notificationPanelNotifications", notifications);
        model.addAttribute("notificationPanelUsersById", usersById);
        model.addAttribute("notificationPanelProductsById", productsById);
        model.addAttribute("notificationPanelFilter", filterLabel);
        model.addAttribute("notificationPanelUnreadCount", notificationService.countUnread(userId));
    }

    private int parsePageParam(final String pageParam) {
        if (pageParam == null) {
            return 1;
        }
        try {
            final int page = Integer.parseInt(pageParam);
            return Math.max(page, 1);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private NotificationType parseFilter(final String filterParam) {
        if (filterParam == null || filterParam.trim().isEmpty() || "ALL".equalsIgnoreCase(filterParam)) {
            return null;
        }
        try {
            return NotificationType.valueOf(filterParam.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
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
