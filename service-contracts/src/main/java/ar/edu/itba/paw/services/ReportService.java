package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Report;

public interface ReportService {
    Report report(long productId, long reporterUserId, long reportedUserId);
    boolean hasReported(long productId, long reporterUserId);
    PaginatedResult<ReportedProductSummary> findAllGroupedByProduct(int page, int pageSize);
    void deleteByProductId(long productId);
}
