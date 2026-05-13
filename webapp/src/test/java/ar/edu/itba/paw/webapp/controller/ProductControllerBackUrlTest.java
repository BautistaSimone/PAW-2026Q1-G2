package ar.edu.itba.paw.webapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ProductControllerBackUrlTest {

    @Test
    void usesInternalRefererIncludingQueryString() {
        final MockHttpServletRequest request = requestFrom("http://localhost:8000/profile?tab=reports&reportsPage=2");

        assertEquals("/profile?tab=reports&reportsPage=2", ProductController.resolveProductDetailBackUrl(request, 12L));
    }

    @Test
    void fallsBackWhenProductWasJustCreated() {
        final MockHttpServletRequest request = requestFrom("http://localhost:8000/products/new");
        request.setParameter("created", "1");

        assertEquals("/", ProductController.resolveProductDetailBackUrl(request, 12L));
    }

    @Test
    void fallsBackWhenRefererIsMissing() {
        final MockHttpServletRequest request = requestFrom(null);

        assertEquals("/", ProductController.resolveProductDetailBackUrl(request, 12L));
    }

    @Test
    void fallsBackWhenRefererIsExternal() {
        final MockHttpServletRequest request = requestFrom("https://example.com/profile");

        assertEquals("/", ProductController.resolveProductDetailBackUrl(request, 12L));
    }

    @Test
    void fallsBackWhenRefererIsSchemeRelativeExternal() {
        final MockHttpServletRequest request = requestFrom("//example.com/profile");

        assertEquals("/", ProductController.resolveProductDetailBackUrl(request, 12L));
    }

    @Test
    void fallsBackWhenRefererIsCurrentProductDetail() {
        final MockHttpServletRequest request = requestFrom("http://localhost:8000/products/12?reported=1");

        assertEquals("/", ProductController.resolveProductDetailBackUrl(request, 12L));
    }

    @Test
    void stripsContextPathFromInternalReferer() {
        final MockHttpServletRequest request = requestFrom("http://localhost:8000/app/profile?userId=4");
        request.setContextPath("/app");

        assertEquals("/profile?userId=4", ProductController.resolveProductDetailBackUrl(request, 12L));
    }

    private static MockHttpServletRequest requestFrom(final String referer) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8000);
        if (referer != null) {
            request.addHeader("Referer", referer);
        }
        return request;
    }
}
