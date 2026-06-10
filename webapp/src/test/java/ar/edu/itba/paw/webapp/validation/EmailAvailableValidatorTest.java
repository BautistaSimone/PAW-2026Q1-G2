package ar.edu.itba.paw.webapp.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;

@ExtendWith(MockitoExtension.class)
class EmailAvailableValidatorTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private EmailAvailableValidator validator;

    @Test
    void testIsValidWhenEmailIsEmpty() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));

        verifyNoInteractions(userService);
    }

    @Test
    void testIsValidWhenEmailIsAvailable() {
        when(userService.findByEmail("available@test.com")).thenReturn(Optional.empty());

        assertTrue(validator.isValid("available@test.com", null));
    }

    @Test
    void testIsInvalidWhenEmailExists() {
        when(userService.findByEmail("taken@test.com")).thenReturn(Optional.of(mock(User.class)));

        assertFalse(validator.isValid("taken@test.com", null));
    }
}
