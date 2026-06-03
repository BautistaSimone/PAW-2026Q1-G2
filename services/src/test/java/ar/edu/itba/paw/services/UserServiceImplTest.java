package ar.edu.itba.paw.services;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.UserDao;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProductService productService;

    @Mock
    private ReportService reportService;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserServiceImpl(userDao, passwordEncoder);

        // Inject field-level @Autowired dependencies via reflection
        setField(userService, "productService", productService);
        setField(userService, "reportService", reportService);
    }

    private static void setField(final Object target, final String fieldName, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testFindByIdWhenUserExists() {
        // Arrange
        final User user = new User(
            1L, 
            "test", 
            "test", 
            "test", 
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
            null
            );
        Mockito.when(userDao.findById(1L)).thenReturn(Optional.of(user));

        // Excercise
        final Optional<User> result = userService.findById(1L);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1L, result.get().getId());
    }

    @Test
    public void testFindByIdWhenUserNotExists() {
        // Arrange
        Mockito.when(userDao.findById(Mockito.anyLong())).thenReturn(Optional.empty());
 
        // Excercise
        final Optional<User> result = userService.findById(1L);

        // Assert
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    public void testBanCallsBulkMethodsAndNotPerProductIteration() {
        // Arrange
        final Long userId = 42L;

        // Act
        userService.ban(userId);

        // Assert — bulk methods called exactly once
        Mockito.verify(productService, Mockito.times(1)).hideAllProductsByAdmin(userId);
        Mockito.verify(reportService, Mockito.times(1)).deleteByOwnerUserId(userId);
        Mockito.verify(userDao, Mockito.times(1)).ban(userId);

        // Assert — old per-product iteration methods are NEVER called
        Mockito.verify(productService, Mockito.never()).listProducts(Mockito.any(ar.edu.itba.paw.models.ProductSearchCriteria.class));
        Mockito.verify(productService, Mockito.never()).hideProductByAdmin(Mockito.anyLong());
        Mockito.verify(reportService, Mockito.never()).deleteByProductId(Mockito.anyLong());
    }
}