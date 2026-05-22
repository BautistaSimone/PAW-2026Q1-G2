package ar.edu.itba.paw.webapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import ar.edu.itba.paw.webapp.Util;

class UtilBackUrlTest {

    @Test
    void usesInternalRefererIncludingQueryString() {
        final MockHttpServletRequest request = requestFrom("http://localhost:8000/profile?tab=reports&reportsPage=2");

        assertEquals("/profile?tab=reports&reportsPage=2", Util.resolveBackUrl(request));
    }

    @Test
    void fallsBackWhenProductWasJustCreated() {
        final MockHttpServletRequest request = requestFrom("http://localhost:8000/products/new");
        request.setParameter("created", "1");

        assertEquals("/", Util.resolveBackUrl(request));
    }

    @Test
    void fallsBackWhenRefererIsMissing() {
        final MockHttpServletRequest request = requestFrom(null);

        assertEquals("/", Util.resolveBackUrl(request));
    }

    @Test
    void fallsBackWhenRefererIsExternal() {
        final MockHttpServletRequest request = requestFrom("https://example.com/profile");

        assertEquals("/", Util.resolveBackUrl(request));
    }

    @Test
    void fallsBackWhenRefererIsSchemeRelativeExternal() {
        final MockHttpServletRequest request = requestFrom("//example.com/profile");

        assertEquals("/", Util.resolveBackUrl(request));
    }

    @Test
    void stripsContextPathFromInternalReferer() {
        final MockHttpServletRequest request = requestFrom("http://localhost:8000/app/profile?userId=4");
        request.setContextPath("/app");

        assertEquals("/profile?userId=4", Util.resolveBackUrl(request));
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
