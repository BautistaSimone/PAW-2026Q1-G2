package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Report;

public interface ReportDao {
    Report create(long productId, long ownerUserId, long reporterUserId);
    boolean existsByProductAndReporter(long productId, long reporterUserId);
    PaginatedResult<ReportedProductProjection> findAllGroupedByProduct(int page, int pageSize);
    void deleteByProductId(long productId);
}
