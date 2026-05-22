package ar.edu.itba.paw.webapp.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PaginationValidationTest {

    @Test
    void homeRejectsNegativePage() {
        final HomeController controller = new HomeController(null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            controller.home(null, null, null, null, null, null, null, null, -1)
        );
    }

    @Test
    void profileRejectsNegativePage() {
        final UserController controller = new UserController(null, null, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            controller.profile(null, null, -1, 1, null, null)
        );
    }

    @Test
    void profileRejectsNegativeTrashPage() {
        final UserController controller = new UserController(null, null, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            controller.profile(null, null, 1, -1, null, null)
        );
    }

    @Test
    void trashRejectsNegativePage() {
        final UserController controller = new UserController(null, null, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            controller.trash(null, -1)
        );
    }
}
