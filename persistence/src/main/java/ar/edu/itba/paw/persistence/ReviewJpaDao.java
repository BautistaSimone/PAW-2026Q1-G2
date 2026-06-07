package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.models.User;

@Repository
public class ReviewJpaDao implements ReviewDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Review create(long purchaseId, long sellerId, long buyerId, int score, String text) {
        final Review review = new Review(purchaseId, sellerId, buyerId, score, text, LocalDateTime.now());
        em.persist(review);
        return review;
    }

    @Override
    public Optional<Review> findByPurchaseId(long purchaseId) {
        final List<Review> reviews = em.createQuery(
                "FROM Review r WHERE r.purchaseId = :purchaseId", Review.class).setParameter("purchaseId", purchaseId)
                .getResultList();

        if (reviews.isEmpty()) {
            return Optional.empty();
        }

        final Review review = reviews.get(0);
        populateBuyerUsername(review);
        return Optional.of(review);
    }

    @Override
    public Set<Long> findReviewedPurchaseIds(final Set<Long> purchaseIds) {
        if (purchaseIds == null || purchaseIds.isEmpty()) {
            return Collections.emptySet();
        }
        return em.createQuery(
                "SELECT r.purchaseId FROM Review r WHERE r.purchaseId IN :purchaseIds",
                Long.class)
                .setParameter("purchaseIds", purchaseIds)
                .getResultList()
                .stream()
                .collect(Collectors.toSet());
    }

    @Override
    public PaginatedResult<Review> findBySellerId(long sellerId, int page, int pageSize) {
        final int safePage = page < 1 ? 1 : page;
        final int safePageSize = pageSize < 1 ? 10 : pageSize;

        final long totalCount = em.createQuery(
                "SELECT COUNT(r) FROM Review r WHERE r.sellerId = :sellerId", Long.class)
                .setParameter("sellerId", sellerId)
                .getSingleResult();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        final List<Review> reviews = em.createQuery(
                "FROM Review r WHERE r.sellerId = :sellerId ORDER BY r.createdAt DESC", Review.class)
                .setParameter("sellerId", sellerId)
                .setMaxResults(safePageSize)
                .setFirstResult((safePage - 1) * safePageSize)
                .getResultList();

        if (!reviews.isEmpty()) {
            final List<Long> buyerIds = reviews.stream()
                    .map(Review::getBuyerId)
                    .distinct()
                    .collect(Collectors.toList());

            final Map<Long, String> usernames = em
                    .createQuery("SELECT u.id, u.username FROM User u WHERE u.id IN :ids", Object[].class)
                    .setParameter("ids", buyerIds)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toMap(res -> (Long) res[0], res -> (String) res[1]));

            for (Review review : reviews) {
                review.setBuyerUsername(usernames.get(review.getBuyerId()));
            }
        }

        return new PaginatedResult<>(reviews, page, pageSize, totalCount);
    }

    @Override
    public SellerRatingSummary summaryForSeller(long sellerId) {
        final Object[] result = em.createQuery(
                "SELECT COALESCE(AVG(r.score), 0.0), COUNT(r) FROM Review r WHERE r.sellerId = :sellerId",
                Object[].class).setParameter("sellerId", sellerId)
                .getSingleResult();

        final double avgScore = ((Number) result[0]).doubleValue();
        final int count = ((Number) result[1]).intValue();
        return new SellerRatingSummary(avgScore, count);
    }

    @Override
    public Map<Long, SellerRatingSummary> sellerRatingByUserId(final Set<Long> distinctSellerIds) {
        final Map<Long, SellerRatingSummary> result = new HashMap<>();
        if (distinctSellerIds == null || distinctSellerIds.isEmpty()) {
            return result;
        }
        for (Long sellerId : distinctSellerIds) {
            if (sellerId != null) {
                result.put(sellerId, new SellerRatingSummary(0.0, 0));
            }
        }
        if (result.isEmpty()) {
            return result;
        }

        em.createQuery(
                "SELECT r.sellerId AS id, COALESCE(AVG(r.score), 0.0) AS average, COUNT(r) AS count "
                        + "FROM Review r WHERE r.sellerId IN :sellerIds GROUP BY r.sellerId",
                Tuple.class)
                .setParameter("sellerIds", result.keySet())
                .getResultList()
                .forEach(tuple -> result.put(
                        ((Number) tuple.get("id")).longValue(),
                        new SellerRatingSummary(
                                ((Number) tuple.get("average")).doubleValue(),
                                ((Number) tuple.get("count")).intValue())));

        return result;
    }

    private void populateBuyerUsername(Review review) {
        final User buyer = em.find(User.class, review.getBuyerId());
        if (buyer != null) {
            review.setBuyerUsername(buyer.getUsername());
        }
    }
}
