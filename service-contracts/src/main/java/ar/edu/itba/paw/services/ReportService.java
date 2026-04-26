package ar.edu.itba.paw.services;

import java.util.List;

import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;

public interface ReportService {
    Report report(long productId, long reporterUserId);
    boolean hasReported(long productId, long reporterUserId);
    List<ReportedProduct> findAllGroupedByProduct();
    void deleteByProductId(long productId);
}
