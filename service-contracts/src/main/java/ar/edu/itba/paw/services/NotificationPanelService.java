package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Map;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

public interface NotificationPanelService {

    PanelData getPanelData(Long userId, NotificationType filter, int page, int pageSize);

    final class PanelData {

        private final PaginatedResult<Notification> page;
        private final List<Notification> notifications;
        private final Map<Long, User> usersById;
        private final Map<Long, Product> productsById;
        private final String filter;
        private final long unreadCount;

        public PanelData(
                final PaginatedResult<Notification> page,
                final List<Notification> notifications,
                final Map<Long, User> usersById,
                final Map<Long, Product> productsById,
                final String filter,
                final long unreadCount) {
            this.page = page;
            this.notifications = notifications;
            this.usersById = usersById;
            this.productsById = productsById;
            this.filter = filter;
            this.unreadCount = unreadCount;
        }

        public PaginatedResult<Notification> getPage() {
            return page;
        }

        public List<Notification> getNotifications() {
            return notifications;
        }

        public Map<Long, User> getUsersById() {
            return usersById;
        }

        public Map<Long, Product> getProductsById() {
            return productsById;
        }

        public String getFilter() {
            return filter;
        }

        public long getUnreadCount() {
            return unreadCount;
        }
    }
}