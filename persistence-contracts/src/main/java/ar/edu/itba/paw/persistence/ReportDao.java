package ar.edu.itba.paw.persistence;

import java.util.List;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;

public interface ReportDao {
    Report create(long productId, long ownerUserId, long reporterUserId);
    boolean existsByProductAndReporter(long productId, long reporterUserId);
    PaginatedResult<ReportedProduct> findAllGroupedByProduct(int page, int pageSize);
    void deleteByProductId(long productId);
}
