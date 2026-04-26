package ar.edu.itba.paw.persistence;

import java.util.List;

import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;

public interface ReportDao {
    Report create(long productId, long ownerUserId, long reporterUserId);
    boolean existsByProductAndReporter(long productId, long reporterUserId);
    List<ReportedProduct> findAllGroupedByProduct();
    void deleteByProductId(long productId);
}
