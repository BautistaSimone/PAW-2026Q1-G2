package ar.edu.itba.paw.webapp.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.UserService;

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

    @Test
    void communityProductsReturnsEmptyJsonForSellerWithoutActiveProducts() {
        final UserService userService = proxy(UserService.class, (proxy, method, args) -> {
            if ("findById".equals(method.getName())) {
                return Optional.of(new User(
                    1L, "seller@test.com", "password", "seller", false, true, false,
                    null, null, null, null, null, null, null, null
                ));
            }
            throw new UnsupportedOperationException(method.getName());
        });
        final ProductService productService = proxy(ProductService.class, (proxy, method, args) -> {
            if ("listActiveProductsByUser".equals(method.getName())) {
                return new PaginatedResult<>(List.of(), 1, 4, 0);
            }
            throw new UnsupportedOperationException(method.getName());
        });
        final ImageService imageService = proxy(ImageService.class, (proxy, method, args) -> {
            if ("findProductIdsWithImages".equals(method.getName())) {
                return Set.of();
            }
            throw new UnsupportedOperationException(method.getName());
        });
        final HomeController controller = new HomeController(productService, imageService, null, userService, null);

        final ResponseEntity<HomeController.CommunityProductsResponse> response =
            controller.communityUserProducts(1L, 1, null);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getProducts().isEmpty());
        assertEquals(1, response.getBody().getCurrentPage());
        assertEquals(0, response.getBody().getTotalPages());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
    }
}
