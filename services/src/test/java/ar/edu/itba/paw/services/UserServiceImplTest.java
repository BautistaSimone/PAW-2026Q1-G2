package ar.edu.itba.paw.services;

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
    private NotificationService notificationService;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserServiceImpl(userDao, passwordEncoder, productService, notificationService);
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
        Mockito.verify(userDao, Mockito.times(1)).ban(userId);

        // Assert — old per-product iteration methods are NEVER called
        Mockito.verify(productService, Mockito.never()).listProducts(Mockito.any(ar.edu.itba.paw.models.ProductSearchCriteria.class));
        Mockito.verify(productService, Mockito.never()).hideProductByAdmin(Mockito.anyLong());
    }

    @Test
    public void testCreateUserIfEmailAvailableReturnsEmptyForDuplicateEmailBeforeEncodingPassword() {
        final User existing = new User(
            1L,
            "taken@test.com",
            "password",
            "taken",
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
        Mockito.when(userDao.findByEmail("taken@test.com")).thenReturn(Optional.of(existing));

        final Optional<User> result = userService.createUserIfEmailAvailable(
            " taken@test.com ",
            "password",
            "user",
            false,
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

        Assertions.assertFalse(result.isPresent());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.anyString());
        Mockito.verify(userDao, Mockito.never()).createUser(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyBoolean(),
            Mockito.anyBoolean(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        );
    }

    @Test
    public void testToggleFollowFollowsWhenNotFollowing() {
        // Arrange
        Mockito.when(userDao.isFollowing(1L, 2L)).thenReturn(false);

        // Act
        userService.toggleFollow(1L, 2L);

        // Assert
        Mockito.verify(userDao, Mockito.times(1)).follow(1L, 2L);
        Mockito.verify(userDao, Mockito.never()).unfollow(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(notificationService, Mockito.times(1)).notifyFollow(2L, 1L);
    }

    @Test
    public void testToggleFollowUnfollowsWhenAlreadyFollowing() {
        // Arrange
        Mockito.when(userDao.isFollowing(1L, 2L)).thenReturn(true);

        // Act
        userService.toggleFollow(1L, 2L);

        // Assert
        Mockito.verify(userDao, Mockito.times(1)).unfollow(1L, 2L);
        Mockito.verify(userDao, Mockito.never()).follow(Mockito.anyLong(), Mockito.anyLong());
        Mockito.verify(notificationService, Mockito.never()).notifyFollow(Mockito.anyLong(), Mockito.anyLong());
    }
}
