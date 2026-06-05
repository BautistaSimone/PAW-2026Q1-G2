package ar.edu.itba.paw.services;

import java.util.*;
import java.util.stream.Collectors;

import ar.edu.itba.paw.models.PendingNotification;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.PendingNotificationDao;
import ar.edu.itba.paw.persistence.ProductDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendingNotificationServiceImpl implements PendingNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PendingNotificationServiceImpl.class);

    private final PendingNotificationDao pendingNotificationDao;
    private final ProductDao productDao;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public PendingNotificationServiceImpl(
            final PendingNotificationDao pendingNotificationDao,
            final ProductDao productDao,
            @Lazy final UserService userService,
            final EmailService emailService) {
        this.pendingNotificationDao = pendingNotificationDao;
        this.productDao = productDao;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void enqueueForFollowers(Long sellerUserId, Long productId) {
        pendingNotificationDao.createForAllFollowersOf(sellerUserId, productId);
        LOGGER.debug("Enqueued notifications for followers of user {} for product {}", sellerUserId, productId);
    }

    @Override
    @Transactional
    public void processAndSendDigestEmails() {
        final List<PendingNotification> allPending = pendingNotificationDao.findAll();

        if (allPending.isEmpty()) {
            LOGGER.debug("No pending notifications to process");
            return;
        }

        final Map<Long, List<PendingNotification>> byUser = allPending.stream()
                .collect(Collectors.groupingBy(PendingNotification::getFollowerUserId));

        final Set<Long> allProductIds = allPending.stream()
                .map(PendingNotification::getProductId)
                .collect(Collectors.toSet());

        final Map<Long, Product> productMap = productDao.findByIds(allProductIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (existing, replacement) -> existing));

        final List<Long> processedIds = new ArrayList<>();

        for (Map.Entry<Long, List<PendingNotification>> entry : byUser.entrySet()) {
            final Long userId = entry.getKey();
            final List<PendingNotification> userNotifs = entry.getValue();

            final Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                userNotifs.forEach(n -> processedIds.add(n.getNotificationId()));
                continue;
            }

            final User user = userOpt.get();

            final List<Product> products = userNotifs.stream()
                    .map(n -> productMap.get(n.getProductId()))
                    .filter(Objects::nonNull)
                    .filter(p -> "ACTIVE".equals(p.getState()))
                    .collect(Collectors.toList());

            if (!products.isEmpty()) {
                try {
                    emailService.sendNewVinylDigestEmail(
                            user.getEmail(),
                            user.getUsername(),
                            products,
                            LocaleContextHolder.getLocale()
                    );
                    LOGGER.info("Sent digest email to user {} with {} products", userId, products.size());
                } catch (Exception e) {
                    LOGGER.error("Failed to send digest email to user {}", userId, e);
                }
            }

            userNotifs.forEach(n -> processedIds.add(n.getNotificationId()));
        }

        if (!processedIds.isEmpty()) {
            pendingNotificationDao.deleteByIds(processedIds);
            LOGGER.info("Deleted {} processed pending notifications", processedIds.size());
        }
    }
}
