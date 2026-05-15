package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;

@Repository
public class ReportJpaDao implements ReportDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Report create(final long productId, final long ownerUserId, final long reporterUserId) {
        final Report report = new Report(productId, ownerUserId, reporterUserId);
        em.persist(report);
        return report;
    }

    @Override
    public boolean existsByProductAndReporter(final long productId, final long reporterUserId) {
        final long count = em.createQuery(
            "SELECT COUNT(r) FROM Report r WHERE r.productId = :productId AND r.reporterUserId = :reporterUserId",
            Long.class
        ).setParameter("productId", productId)
        .setParameter("reporterUserId", reporterUserId)
        .getSingleResult();

        return count > 0;
    }

    @Override
    public PaginatedResult<ReportedProduct> findAllGroupedByProduct(int page, int pageSize) {
        final int offset = (page - 1) * pageSize;

        final long totalDistinct = em.createQuery(
            "SELECT COUNT(DISTINCT r.productId) FROM Report r", Long.class
        ).getSingleResult();

        final int total = (int) totalDistinct;
        final int maxPage = (int) Math.ceil((double) total / pageSize);

        if (total == 0) {
            return new PaginatedResult<>(Collections.emptyList(), page, maxPage, total);
        }

        @SuppressWarnings("unchecked")
        final List<Object[]> rows = em.createQuery(
            "SELECT r.productId, r.ownerUserId, COUNT(r), p.title, p.artist, u.username " +
            "FROM Report r " +
            "JOIN Product p ON r.productId = p.productId " +
            "JOIN User u ON r.ownerUserId = u.id " +
            "GROUP BY r.productId, r.ownerUserId, p.title, p.artist, u.username " +
            "ORDER BY COUNT(r) DESC"
        ).setMaxResults(pageSize)
        .setFirstResult(offset)
        .getResultList();

        final List<ReportedProduct> results = rows.stream().map(row ->
            new ReportedProduct(
                ((Number) row[0]).longValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).intValue(),
                (String) row[3],
                (String) row[4],
                (String) row[5]
            )
        ).toList();

        return new PaginatedResult<>(results, page, maxPage, total);
    }

    @Override
    public void deleteByProductId(final long productId) {
        em.createQuery("DELETE FROM Report WHERE productId = :productId")
            .setParameter("productId", productId)
            .executeUpdate();
    }
}
