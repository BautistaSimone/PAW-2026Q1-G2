package ar.edu.itba.paw.webapp.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.services.NotificationPanelService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;

@ControllerAdvice
public class NotificationPanelAdvice {

    private static final int PANEL_PAGE_SIZE = 8;

    private final NotificationPanelService notificationPanelService;

    @Autowired
    public NotificationPanelAdvice(
            final NotificationPanelService notificationPanelService) {
        this.notificationPanelService = notificationPanelService;
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

        final Long userId = authUser.getUser().getId();
        final NotificationPanelService.PanelData panelData =
                notificationPanelService.getPanelData(userId, filter, page, PANEL_PAGE_SIZE);

        model.addAttribute("notificationPanelPage", panelData.getPage());
        model.addAttribute("notificationPanelNotifications", panelData.getNotifications());
        model.addAttribute("notificationPanelUsersById", panelData.getUsersById());
        model.addAttribute("notificationPanelProductsById", panelData.getProductsById());
        model.addAttribute("notificationPanelFilter", panelData.getFilter());
        model.addAttribute("notificationPanelUnreadCount", panelData.getUnreadCount());
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
}
