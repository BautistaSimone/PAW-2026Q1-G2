package ar.edu.itba.paw.services;

import java.util.List;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;

public interface ReportService {
    Report report(long productId, long reporterUserId, long reportedUserId);
    boolean hasReported(long productId, long reporterUserId);
    PaginatedResult<ReportedProduct> findAllGroupedByProduct(int page, int pageSize);
    void deleteByProductId(long productId);
}
