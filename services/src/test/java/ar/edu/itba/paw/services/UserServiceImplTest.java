package ar.edu.itba.paw.services;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.UserDao;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;


    // @BeforeEach
    // void setUp() {
    //     userDao = Mockito.mock(UserDao.class);
    //     userService = new UserServiceImpl(userDao);
    // }

    @Test
    public void testFindByIdWhenUserExists() {
        // Arrange
        final User user = new User(1L, "test", "test", "test");
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
}