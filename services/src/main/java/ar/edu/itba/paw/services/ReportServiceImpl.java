package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.ReportedProduct;
import ar.edu.itba.paw.persistence.ReportDao;
import ar.edu.itba.paw.persistence.ProductDao;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportDao reportDao;
    private final ProductDao productDao;

    @Autowired
    EmailService emailService;

    @Autowired
    UserService userService;

    @Autowired
    public ReportServiceImpl(final ReportDao reportDao, final ProductDao productDao) {
        this.reportDao = reportDao;
        this.productDao = productDao;
    }

    @Override
    @Transactional
    public Report report(long productId, long reporterUserId, long reportedUserId) {
        final Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getUserId().equals(reporterUserId)) {
            throw new IllegalArgumentException("Cannot report your own product");
        }

        if (reportDao.existsByProductAndReporter(productId, reporterUserId)) {
            throw new IllegalStateException("Already reported this product");
        }

        User reporter = userService.findById(reporterUserId)
            .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        User seller = userService.findById(reportedUserId)
            .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        emailService.sendProductReportEmail(product, reporter, seller);

        return reportDao.create(productId, product.getUserId(), reporterUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReported(final long productId, final long reporterUserId) {
        return reportDao.existsByProductAndReporter(productId, reporterUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<ReportedProduct> findAllGroupedByProduct(int page, int pageSize) {
        return reportDao.findAllGroupedByProduct(page, pageSize);
    }

    @Override
    @Transactional
    public void deleteByProductId(final long productId) {
        reportDao.deleteByProductId(productId);
    }
}
