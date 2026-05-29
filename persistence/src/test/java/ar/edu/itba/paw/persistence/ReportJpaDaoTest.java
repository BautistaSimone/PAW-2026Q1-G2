package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ReportJpaDaoTest {

    @Autowired
    private ReportJpaDao reportDao;

    @Autowired
    private UserJpaDao userDao;

    @Autowired
    private ProductJpaDao productDao;

    @PersistenceContext
    private EntityManager em;

    private long ownerId;
    private long reporterId;
    private long otherReporterId;
    private long productId;
    private long otherProductId;

    @BeforeEach
    public void setUp() {
        final User owner = userDao.createUser("report-owner@test.com", "pass", "Owner",
            false, true, null, null, null, null, null, null, null, null);
        final User reporter = userDao.createUser("report-reporter@test.com", "pass", "Reporter",
            false, true, null, null, null, null, null, null, null, null);
        final User otherReporter = userDao.createUser("report-other@test.com", "pass", "Other Reporter",
            false, true, null, null, null, null, null, null, null, null);

        ownerId = owner.getId();
        reporterId = reporter.getId();
        otherReporterId = otherReporter.getId();

        final Product product = productDao.createProduct(
            ownerId, "Reported Album", "Reported Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000), 1
        );
        final Product otherProduct = productDao.createProduct(
            ownerId, "Other Reported Album", "Other Artist", "Label", "CAT2", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000), 1
        );

        productId = product.getId();
        otherProductId = otherProduct.getId();
        em.flush();
    }

    @Test
    public void testCreatePersistsReport() {

        // Arrange

        // Act
        final Report report = reportDao.create(productId, ownerId, reporterId);
        em.flush();

        // Assert
        Long count = em.createQuery(
            "SELECT COUNT(r) FROM Report r",
            Long.class
        ).getSingleResult();

        Assertions.assertEquals(1L, count);
    }

    @Test
    public void testExistsByProductAndReporter() {

        // Arrange

        // Act
        final Report report = reportDao.create(productId, ownerId, reporterId);
        em.flush();

        // Assert
        Assertions.assertTrue(reportDao.existsByProductAndReporter(productId, reporterId));
        Assertions.assertFalse(reportDao.existsByProductAndReporter(productId, otherReporterId));
    }

    @Test
    public void testFindAllGroupedByProductReturnsReportCountsAndProductMetadata() {

        // Arrange
        reportDao.create(productId, ownerId, reporterId);
        reportDao.create(productId, ownerId, otherReporterId);
        reportDao.create(otherProductId, ownerId, reporterId);
        em.flush();

        // Act
        final List<ReportedProduct> reportedProducts = reportDao.findAllGroupedByProduct(1, 10).getResults();

        // Assert
        Assertions.assertEquals(2, reportedProducts.size());
        final ReportedProduct mostReported = reportedProducts.get(0);
        Assertions.assertEquals(productId, mostReported.getProductId());
        Assertions.assertEquals(ownerId, mostReported.getOwnerUserId());
        Assertions.assertEquals(2, mostReported.getReportCount());
        Assertions.assertEquals("Reported Album", mostReported.getProductTitle());
        Assertions.assertEquals("Reported Artist", mostReported.getProductArtist());
        Assertions.assertEquals("Owner", mostReported.getOwnerUsername());
    }

    @Test
    public void testDeleteByProductIdDeletesOnlyReportsForThatProduct() {
        // Arrange
        reportDao.create(productId, ownerId, reporterId);
        reportDao.create(productId, ownerId, otherReporterId);
        reportDao.create(otherProductId, ownerId, reporterId);
        em.flush();

        // Act
        reportDao.deleteByProductId(productId);
        em.flush();

        // Assert
        Assertions.assertFalse(reportDao.existsByProductAndReporter(productId, reporterId));
        Assertions.assertFalse(reportDao.existsByProductAndReporter(productId, otherReporterId));
        Assertions.assertTrue(reportDao.existsByProductAndReporter(otherProductId, reporterId));
    }
}
