package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

@Repository
public class PurchaseJpaDao implements PurchaseDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Purchase createPurchase(Long productId, Long buyerId, Long sellerId,
                                   PurchaseStatus status, String buyerToken, String sellerToken,
                                   LocalDateTime reservedUntil) {
        final Purchase purchase = new Purchase(
            productId, buyerId, sellerId, LocalDate.now(), status, buyerToken, sellerToken
        );
        purchase.setReservedUntil(reservedUntil);
        em.persist(purchase);
        return purchase;
    }

    @Override
    public Optional<Purchase> findById(Long purchaseId) {
        return Optional.ofNullable(em.find(Purchase.class, purchaseId));
    }

    private void buildStatusFilter(StringBuilder jpql, List<PurchaseStatus> statuses,
                                   List<String> paramNames, List<Object> paramValues) {
        if (statuses == null || statuses.isEmpty()) {
            return;
        }
        jpql.append(" AND (");
        for (int i = 0; i < statuses.size(); i++) {
            if (i > 0) jpql.append(" OR ");
            final String paramName = "statusPattern" + i;
            jpql.append("p.status = :").append(paramName);
            paramNames.add(paramName);
            paramValues.add(statuses.get(i));
        }
        jpql.append(")");
    }

    @Override
    public PaginatedResult<Purchase> findByBuyerId(Long buyerId, List<PurchaseStatus> statuses, int page, int pageSize) {
        final int safePage = page < 1 ? 1 : page;
        final int safePageSize = pageSize < 1 ? 12 : pageSize;

        final StringBuilder whereJpql = new StringBuilder("WHERE p.buyerId = :buyerId");
        final List<String> paramNames = new ArrayList<>();
        final List<Object> paramValues = new ArrayList<>();
        paramNames.add("buyerId");
        paramValues.add(buyerId);
        buildStatusFilter(whereJpql, statuses, paramNames, paramValues);

        final TypedQuery<Long> countQuery = em.createQuery(
            "SELECT COUNT(p) FROM Purchase p " + whereJpql, Long.class
        );
        for (int i = 0; i < paramNames.size(); i++) {
            countQuery.setParameter(paramNames.get(i), paramValues.get(i));
        }
        final long totalCount = countQuery.getSingleResult();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }
	
	// No need for 1 + 1 query pattern since we are just filtering by id
        final TypedQuery<Purchase> selectQuery = em.createQuery(
            "SELECT p FROM Purchase p " + whereJpql + " ORDER BY p.date DESC", Purchase.class
        );
        for (int i = 0; i < paramNames.size(); i++) {
            selectQuery.setParameter(paramNames.get(i), paramValues.get(i));
        }
        selectQuery.setMaxResults(safePageSize);
        selectQuery.setFirstResult((safePage - 1) * safePageSize);

        return new PaginatedResult<>(selectQuery.getResultList(), page, pageSize, totalCount);
    }

    @Override
    public PaginatedResult<Purchase> findBySellerId(Long sellerId, List<PurchaseStatus> statuses, int page, int pageSize) {
        final int safePage = page < 1 ? 1 : page;
        final int safePageSize = pageSize < 1 ? 12 : pageSize;

        final StringBuilder whereJpql = new StringBuilder("WHERE p.sellerId = :sellerId");
        final List<String> paramNames = new ArrayList<>();
        final List<Object> paramValues = new ArrayList<>();
        paramNames.add("sellerId");
        paramValues.add(sellerId);
        buildStatusFilter(whereJpql, statuses, paramNames, paramValues);

        final TypedQuery<Long> countQuery = em.createQuery(
            "SELECT COUNT(p) FROM Purchase p " + whereJpql, Long.class
        );
        for (int i = 0; i < paramNames.size(); i++) {
            countQuery.setParameter(paramNames.get(i), paramValues.get(i));
        }
        final long totalCount = countQuery.getSingleResult();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        final TypedQuery<Purchase> selectQuery = em.createQuery(
            "SELECT p FROM Purchase p " + whereJpql + " ORDER BY p.date DESC", Purchase.class
        );
        for (int i = 0; i < paramNames.size(); i++) {
            selectQuery.setParameter(paramNames.get(i), paramValues.get(i));
        }
        selectQuery.setMaxResults(safePageSize);
        selectQuery.setFirstResult((safePage - 1) * safePageSize);

        return new PaginatedResult<>(selectQuery.getResultList(), page, pageSize, totalCount);
    }

    @Override
    public void updateStatus(Long purchaseId, PurchaseStatus status) {
        final Purchase purchase = em.find(Purchase.class, purchaseId);
        if (purchase == null) {
            throw new IllegalArgumentException("Purchase not found");
        }
        purchase.setStatus(status);
    }

    @Override
    public List<Purchase> findExpiredPending(final LocalDateTime now) {
        return em.createQuery(
            "FROM Purchase p WHERE p.reservedUntil IS NOT NULL AND p.reservedUntil < :now AND p.status = :status",
            Purchase.class)
            .setParameter("now", now)
            .setParameter("status", PurchaseStatus.PENDING)
            .getResultList();
    }
}
