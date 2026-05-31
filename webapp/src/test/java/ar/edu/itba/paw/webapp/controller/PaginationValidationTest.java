package ar.edu.itba.paw.webapp.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Test
    void communityProductsRejectsNegativePage() {
        final HomeController controller = new HomeController(null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            controller.communityUserProducts(1L, -1, null)
        );
    }

    @Test
    void communityProductsJsonDoesNotExposeSensitiveUserFields() throws Exception {
        final HomeController.CommunityProductDto product =
            new HomeController.CommunityProductDto(1L, "Album", "Artist", "$1.000", "/products/1", null);
        final HomeController.CommunityProductsResponse response =
            new HomeController.CommunityProductsResponse(List.of(product), 1, 1, false, false);

        final String json = new ObjectMapper().writeValueAsString(response);

        assertTrue(json.contains("Album"));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("email"));
        assertFalse(json.contains("streetName"));
        assertFalse(json.contains("banned"));
        assertFalse(json.contains("enabled"));
        assertFalse(json.contains("mod"));
    }
}
