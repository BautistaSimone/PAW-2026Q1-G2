package ar.edu.itba.paw.webapp;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;

public final class Util {

    private Util() {
        // Prevent instantiation
    }

    /**
     * Maps each product that has at least one image to its image URL ({@code /images/product/{id}}).
     * URL/route construction is a presentation concern, so it lives here; the data on which products
     * have images comes from the service layer.
     */
    public static Map<Long, String> productImageUrls(final ImageService imageService, final List<Product> products) {
        final Map<Long, String> productImageUrls = new HashMap<>();
        if (products == null || products.isEmpty()) {
            return productImageUrls;
        }

        final List<Long> productIds = new ArrayList<>();
        for (Product product : products) {
            productIds.add(product.getId());
        }
        final Set<Long> productIdsWithImages = imageService.findProductIdsWithImages(productIds);
        for (Long productId : productIdsWithImages) {
            productImageUrls.put(productId, "/images/product/" + productId);
        }
        return productImageUrls;
    }

    public static void refreshAuthenticationPrincipal(final PawAuthUser current, final User refreshedUser) {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        final PawAuthUser newPrincipal = new PawAuthUser(refreshedUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        newPrincipal,
                        currentAuth != null ? currentAuth.getCredentials() : null,
                        newPrincipal.getAuthorities()));
    }

    public static String resolveBackUrl(final HttpServletRequest request) {
        final String backParam = request.getParameter("back");
        if (backParam != null && !backParam.isBlank()) {
            final String trimmed = backParam.trim();
            if (trimmed.startsWith("/") && !trimmed.startsWith("//") && !trimmed.contains("\\")) {
                return trimmed;
            }
        }

        final String fallbackUrl = "/";
        if ("1".equals(request.getParameter("created"))) {
            return fallbackUrl;
        }

        final String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return fallbackUrl;
        }

        try {
            final URI refererUri = URI.create(referer);
            if ((refererUri.isAbsolute() || refererUri.getHost() != null)
                    && !isSameOrigin(refererUri, request)) {
                return fallbackUrl;
            }

            final String backPath = internalPathFromReferer(refererUri.getRawPath(), request.getContextPath());

            final String query = refererUri.getRawQuery();
            return query == null || query.isBlank() ? backPath : backPath + "?" + query;
        } catch (IllegalArgumentException e) {
            return fallbackUrl;
        }
    }

    public static boolean isSameOrigin(final URI refererUri, final HttpServletRequest request) {
        final String refererHost = refererUri.getHost();
        if (refererHost == null || !refererHost.equalsIgnoreCase(request.getServerName())) {
            return false;
        }
        if (refererUri.getScheme() != null && !refererUri.getScheme().equalsIgnoreCase(request.getScheme())) {
            return false;
        }
        return effectivePort(refererUri.getScheme(), refererUri.getPort()) == effectivePort(request.getScheme(),
                request.getServerPort());
    }

    public static int effectivePort(final String scheme, final int port) {
        if (port > 0) {
            return port;
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        return 80;
    }

    public static String internalPathFromReferer(final String rawPath, final String contextPath) {
        String path = rawPath == null || rawPath.isBlank() ? "/" : rawPath;
        if (contextPath != null && !contextPath.isBlank()) {
            if (path.equals(contextPath)) {
                return "/";
            }
            if (!path.startsWith(contextPath + "/")) {
                return null;
            }
            path = path.substring(contextPath.length());
        }
        return path.startsWith("/") ? path : "/" + path;
    }

}
