package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.persistence.ReportDao;
import ar.edu.itba.paw.persistence.ReportedProductProjection;

@Service
public class ReportServiceImpl implements ReportService {

    @Value("${mail.username}")
    private String adminEmail;

    private final ReportDao reportDao;
    private final ProductService productService;
    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public ReportServiceImpl(final ReportDao reportDao, final ProductService productService,
            final EmailService emailService, @Lazy final UserService userService) {
        this.reportDao = reportDao;
        this.productService = productService;
        this.emailService = emailService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Report report(long productId, long reporterUserId) {
        final Product product = productService.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getUserId().equals(reporterUserId)) {
            throw new IllegalArgumentException("Cannot report your own product");
        }

        if (reportDao.existsByProductAndReporter(productId, reporterUserId)) {
            throw new IllegalStateException("Already reported this product");
        }

        User reporter = userService.findById(reporterUserId)
            .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        User seller = userService.findById(product.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        final User admin = userService.findByEmail(adminEmail).orElse(null);
        final java.util.Locale adminLocale = admin != null ? admin.getPreferredLocale() : new java.util.Locale("es");

        emailService.sendProductReportEmail(product, reporter, seller, adminLocale);

        return reportDao.create(productId, product.getUserId(), reporterUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReported(final long productId, final long reporterUserId) {
        return reportDao.existsByProductAndReporter(productId, reporterUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<ReportedProductSummary> findAllGroupedByProduct(int page, int pageSize) {
        final PaginatedResult<ReportedProductProjection> pageResult = reportDao.findAllGroupedByProduct(page, pageSize);
        final List<ReportedProductSummary> summaries = pageResult.getResults().stream()
            .map(report -> new ReportedProductSummary(
                report.getProductId(),
                report.getOwnerUserId(),
                report.getReportCount(),
                report.getProductTitle(),
                report.getProductArtist(),
                report.getOwnerUsername()
            ))
            .toList();

        return new PaginatedResult<>(summaries, pageResult.getCurrentPage(), pageSize, pageResult.getTotalCount());
    }

    @Override
    @Transactional
    public void deleteByProductId(final long productId) {
        reportDao.deleteByProductId(productId);
    }

    @Override
    @Transactional
    public void deleteByOwnerUserId(final long ownerUserId) {
        reportDao.deleteByOwnerUserId(ownerUserId);
    }
}
