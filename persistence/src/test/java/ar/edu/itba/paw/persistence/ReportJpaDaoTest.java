package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ReportJpaDaoTest {

    @Autowired
    private ReportJpaDao reportDao;

    @PersistenceContext
    private EntityManager em;

    private long ownerId;
    private long reporterId;
    private long otherReporterId;
    private long productId;
    private long otherProductId;

    private User insertUser(final String email, final String username) {
        final User user = new User(
                email,
                "pass",
                username,
                false,
                true,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        em.persist(user);
        em.flush();
        return user;
    }

    private Product insertProduct(final Long ownerId, final String title, final String artist,
            final String catalogNumber) {
        final Product product = new Product(
                ownerId,
                title,
                artist,
                "Label",
                catalogNumber,
                "Argentina",
                Collections.emptyList(),
                "Description",
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(9),
                LocalDate.now(),
                BigDecimal.valueOf(1000),
                1);
        em.persist(product);
        em.flush();
        return product;
    }

    private Report insertReport(final Long productId, final Long ownerUserId, final Long reporterUserId) {
        final Report report = new Report(productId, ownerUserId, reporterUserId);
        em.persist(report);
        em.flush();
        return report;
    }

    @BeforeEach
    public void setUp() {
        final User owner = insertUser("report-owner@test.com", "Owner");
        final User reporter = insertUser("report-reporter@test.com", "Reporter");
        final User otherReporter = insertUser("report-other@test.com", "Other Reporter");

        ownerId = owner.getId();
        reporterId = reporter.getId();
        otherReporterId = otherReporter.getId();

        final Product product = insertProduct(ownerId, "Reported Album", "Reported Artist", "CAT");
        final Product otherProduct = insertProduct(ownerId, "Other Reported Album", "Other Artist", "CAT2");

        productId = product.getId();
        otherProductId = otherProduct.getId();
        em.flush();
    }

    @Test
    public void testCreatePersistsReport() {

        // Arrange

        // Act
        reportDao.create(productId, ownerId, reporterId);
        em.flush();
        em.clear();

        // Assert
        Long count = em.createQuery(
                "SELECT COUNT(r) FROM Report r",
                Long.class).getSingleResult();

        Assertions.assertEquals(1L, count);
    }

    @Test
    public void testExistsByProductAndReporter() {

        // Arrange

        // Act
        insertReport(productId, ownerId, reporterId);
        em.flush();
        em.clear();

        // Assert
        Assertions.assertTrue(reportDao.existsByProductAndReporter(productId, reporterId));
        Assertions.assertFalse(reportDao.existsByProductAndReporter(productId, otherReporterId));
    }

    @Test
    public void testFindAllGroupedByProductReturnsReportCountsAndProductMetadata() {

        // Arrange
        insertReport(productId, ownerId, reporterId);
        insertReport(productId, ownerId, otherReporterId);
        insertReport(otherProductId, ownerId, reporterId);
        em.flush();
        em.clear();

        // Act
        final List<ReportedProductProjection> reportedProducts = reportDao.findAllGroupedByProduct(1, 10).getResults();

        // Assert
        Assertions.assertEquals(2, reportedProducts.size());
        final ReportedProductProjection mostReported = reportedProducts.get(0);
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
        insertReport(productId, ownerId, reporterId);
        insertReport(productId, ownerId, otherReporterId);
        insertReport(otherProductId, ownerId, reporterId);
        em.flush();
        em.clear();

        // Act
        reportDao.deleteByProductId(productId);
        em.flush();
        em.clear();

        // Assert
        final Long productCount = em.createQuery(
                "SELECT COUNT(r) FROM Report r WHERE r.productId = :productId",
                Long.class).setParameter("productId", productId).getSingleResult();
        final Long otherProductCount = em.createQuery(
                "SELECT COUNT(r) FROM Report r WHERE r.productId = :productId",
                Long.class).setParameter("productId", otherProductId).getSingleResult();
        Assertions.assertEquals(0L, productCount.longValue());
        Assertions.assertEquals(1L, otherProductCount.longValue());
    }

    @Test
    public void testDeleteByOwnerUserIdDeletesOnlyReportsForThatOwner() {
        // Arrange
        final User otherOwner = insertUser("report-other-owner@test.com", "OtherOwner");
        final Product otherOwnerProduct = insertProduct(otherOwner.getId(), "Other Owner Album", "Artist", "CAT3");
        em.flush();

        // Reports for the original owner (ownerId) on both their products
        insertReport(productId, ownerId, reporterId);
        insertReport(productId, ownerId, otherReporterId);
        insertReport(otherProductId, ownerId, reporterId);

        // Reports for the other owner
        insertReport(otherOwnerProduct.getId(), otherOwner.getId(), reporterId);
        em.flush();
        em.clear();

        // Act
        reportDao.deleteByOwnerUserId(ownerId);
        em.flush();
        em.clear();

        // Assert — all reports for ownerId are gone
        final Long ownerReports = em.createQuery(
                "SELECT COUNT(r) FROM Report r WHERE r.ownerUserId = :ownerUserId",
                Long.class).setParameter("ownerUserId", ownerId).getSingleResult();
        final Long otherOwnerReports = em.createQuery(
                "SELECT COUNT(r) FROM Report r WHERE r.ownerUserId = :ownerUserId",
                Long.class).setParameter("ownerUserId", otherOwner.getId()).getSingleResult();

        Assertions.assertEquals(0L, ownerReports.longValue());
        Assertions.assertEquals(1L, otherOwnerReports.longValue());
    }
}
