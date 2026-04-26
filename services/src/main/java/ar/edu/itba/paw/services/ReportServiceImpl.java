package ar.edu.itba.paw.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;
import ar.edu.itba.paw.persistence.ReportDao;
import ar.edu.itba.paw.persistence.ProductDao;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportDao reportDao;
    private final ProductDao productDao;

    @Autowired
    public ReportServiceImpl(final ReportDao reportDao, final ProductDao productDao) {
        this.reportDao = reportDao;
        this.productDao = productDao;
    }

    @Override
    public Report report(final long productId, final long reporterUserId) {
        final Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getUserId().equals(reporterUserId)) {
            throw new IllegalArgumentException("Cannot report your own product");
        }

        if (reportDao.existsByProductAndReporter(productId, reporterUserId)) {
            throw new IllegalStateException("Already reported this product");
        }

        return reportDao.create(productId, product.getUserId(), reporterUserId);
    }

    @Override
    public boolean hasReported(final long productId, final long reporterUserId) {
        return reportDao.existsByProductAndReporter(productId, reporterUserId);
    }

    @Override
    public List<ReportedProduct> findAllGroupedByProduct() {
        return reportDao.findAllGroupedByProduct();
    }

    @Override
    public void deleteByProductId(final long productId) {
        reportDao.deleteByProductId(productId);
    }
}
