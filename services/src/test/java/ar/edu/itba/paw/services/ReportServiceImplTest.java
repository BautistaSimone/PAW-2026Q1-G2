package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.ReportDao;
import ar.edu.itba.paw.persistence.ReportedProductProjection;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {

    @InjectMocks
    private ReportServiceImpl reportService;

    @Mock
    private ReportDao reportDao;

    @Mock
    private ProductService productService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        java.lang.reflect.Field field = ReportServiceImpl.class.getDeclaredField("adminEmail");
        field.setAccessible(true);
        field.set(reportService, "admin@vinyland.com");
    }

    @Test
    public void testReportProductNotFound() {
        // Arrange
        Mockito.when(productService.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            reportService.report(10L, 1L);
        });
    }

    @Test
    public void testReportOwnProduct() {
        // Arrange
        Product p = new Product(10L, 1L, "Title", "Artist", "Label", "Catalog", "Country", Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);
        Mockito.when(productService.findById(10L)).thenReturn(Optional.of(p));

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            reportService.report(10L, 1L);
        });
    }

    @Test
    public void testReportAlreadyReported() {
        // Arrange
        Product p = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country", Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);
        Mockito.when(productService.findById(10L)).thenReturn(Optional.of(p));
        Mockito.when(reportDao.existsByProductAndReporter(10L, 1L)).thenReturn(true);

        // Act & Assert
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportService.report(10L, 1L);
        });
    }

    @Test
    public void testReportReporterNotFound() {
        // Arrange
        Product p = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country", Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);
        Mockito.when(productService.findById(10L)).thenReturn(Optional.of(p));
        Mockito.when(reportDao.existsByProductAndReporter(10L, 1L)).thenReturn(false);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            reportService.report(10L, 1L);
        });
    }

    @Test
    public void testReportSellerNotFound() {
        // Arrange
        Product p = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country", Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);
        User reporter = new User(1L, "rep@test.com", "pass", "reporter", false, true, false, null, null, null, null, null, null, null, null);
        Mockito.when(productService.findById(10L)).thenReturn(Optional.of(p));
        Mockito.when(reportDao.existsByProductAndReporter(10L, 1L)).thenReturn(false);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(reporter));
        Mockito.when(userService.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            reportService.report(10L, 1L);
        });
    }

    @Test
    public void testReportSuccess() {
        // Arrange
        Product p = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country", Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);
        User reporter = new User(1L, "rep@test.com", "pass", "reporter", false, true, false, null, null, null, null, null, null, null, null);
        User seller = new User(2L, "sel@test.com", "pass", "seller", false, true, false, null, null, null, null, null, null, null, null);
        Report report = new Report(10L, 2L, 1L);

        Mockito.when(productService.findById(10L)).thenReturn(Optional.of(p));
        Mockito.when(reportDao.existsByProductAndReporter(10L, 1L)).thenReturn(false);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(reporter));
        Mockito.when(userService.findById(2L)).thenReturn(Optional.of(seller));
        Mockito.when(reportDao.create(10L, 2L, 1L)).thenReturn(report);

        // Act
        Report result = reportService.report(10L, 1L);

        // Assert
        Assertions.assertNotNull(result);
        Mockito.verify(emailService, Mockito.times(1))
                .sendProductReportEmail(p, reporter, seller, new Locale("es"));
    }

    @Test
    public void testHasReported() {
        // Arrange
        Mockito.when(reportDao.existsByProductAndReporter(10L, 1L)).thenReturn(true);

        // Act
        boolean result = reportService.hasReported(10L, 1L);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void testFindAllGroupedByProduct() {
        // Arrange
        ReportedProductProjection proj = Mockito.mock(ReportedProductProjection.class);
        Mockito.when(proj.getProductId()).thenReturn(10L);
        Mockito.when(proj.getOwnerUserId()).thenReturn(2L);
        Mockito.when(proj.getReportCount()).thenReturn(3);
        Mockito.when(proj.getProductTitle()).thenReturn("Title");
        Mockito.when(proj.getProductArtist()).thenReturn("Artist");
        Mockito.when(proj.getOwnerUsername()).thenReturn("seller");

        PaginatedResult<ReportedProductProjection> pageResult = new PaginatedResult<>(
                Arrays.asList(proj), 1, 10, 1
        );
        Mockito.when(reportDao.findAllGroupedByProduct(1, 10)).thenReturn(pageResult);

        // Act
        PaginatedResult<ReportedProductSummary> result = reportService.findAllGroupedByProduct(1, 10);

        // Assert
        Assertions.assertEquals(1, result.getResults().size());
        ReportedProductSummary summary = result.getResults().get(0);
        Assertions.assertEquals(10L, summary.getProductId());
        Assertions.assertEquals(2L, summary.getOwnerUserId());
        Assertions.assertEquals(3L, summary.getReportCount());
        Assertions.assertEquals("Title", summary.getProductTitle());
        Assertions.assertEquals("Artist", summary.getProductArtist());
        Assertions.assertEquals("seller", summary.getOwnerUsername());
    }

    @Test
    public void testFindAllGroupedByProductForAdminRejectsNonAdmin() {
        Mockito.when(userService.isAdmin(7L)).thenReturn(false);

        Optional<PaginatedResult<ReportedProductSummary>> result = reportService.findAllGroupedByProductForAdmin(7L, 1, 10);

        Assertions.assertFalse(result.isPresent());
        Mockito.verify(reportDao, Mockito.never()).findAllGroupedByProduct(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void testFindAllGroupedByProductForAdminReturnsReports() {
        ReportedProductProjection proj = Mockito.mock(ReportedProductProjection.class);
        Mockito.when(proj.getProductId()).thenReturn(10L);
        Mockito.when(proj.getOwnerUserId()).thenReturn(2L);
        Mockito.when(proj.getReportCount()).thenReturn(3);
        Mockito.when(proj.getProductTitle()).thenReturn("Title");
        Mockito.when(proj.getProductArtist()).thenReturn("Artist");
        Mockito.when(proj.getOwnerUsername()).thenReturn("seller");

        PaginatedResult<ReportedProductProjection> pageResult = new PaginatedResult<>(
                Arrays.asList(proj), 1, 10, 1
        );
        Mockito.when(userService.isAdmin(7L)).thenReturn(true);
        Mockito.when(reportDao.findAllGroupedByProduct(1, 10)).thenReturn(pageResult);

        Optional<PaginatedResult<ReportedProductSummary>> result = reportService.findAllGroupedByProductForAdmin(7L, 1, 10);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1, result.get().getResults().size());
    }

    @Test
    public void testDeleteByProductId() {
        // Act
        reportService.deleteByProductId(10L);

        // Assert
        Mockito.verify(reportDao, Mockito.times(1)).deleteByProductId(10L);
    }

    @Test
    public void testDeleteByOwnerUserId() {
        // Act
        reportService.deleteByOwnerUserId(2L);

        // Assert
        Mockito.verify(reportDao, Mockito.times(1)).deleteByOwnerUserId(2L);
    }

    @Test
    public void testReportSuccessWithAdminPreferredLocale() {
        // Arrange
        Product p = new Product(10L, 2L, "Title", "Artist", "Label", "Catalog", "Country", Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);
        User reporter = new User(1L, "rep@test.com", "pass", "reporter", false, true, false, null, null, null, null, null, null, null, null);
        User seller = new User(2L, "sel@test.com", "pass", "seller", false, true, false, null, null, null, null, null, null, null, null);
        User admin = new User(3L, "admin@test.com", "pass", "admin", true, true, false, null, null, null, null, null, null, null, null);
        admin.setLanguage("en");
        Report report = new Report(10L, 2L, 1L);

        Mockito.when(productService.findById(10L)).thenReturn(Optional.of(p));
        Mockito.when(reportDao.existsByProductAndReporter(10L, 1L)).thenReturn(false);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(reporter));
        Mockito.when(userService.findById(2L)).thenReturn(Optional.of(seller));
        Mockito.when(userService.findByEmail("admin@vinyland.com")).thenReturn(Optional.of(admin));
        Mockito.when(reportDao.create(10L, 2L, 1L)).thenReturn(report);

        // Act
        Report result = reportService.report(10L, 1L);

        // Assert
        Assertions.assertNotNull(result);
        Mockito.verify(emailService, Mockito.times(1))
                .sendProductReportEmail(p, reporter, seller, Locale.ENGLISH);
    }
}
