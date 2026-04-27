package ar.edu.itba.paw.webapp.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ar.edu.itba.paw.webapp.auth.PawAuthUser;

@Component
public class VerificationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// If the user isn't verified, force them to be
        if (auth != null && auth.getPrincipal() instanceof PawAuthUser) {
            PawAuthUser authUser = (PawAuthUser) auth.getPrincipal();

            if (authUser.getUser() != null && !authUser.getUser().getEnabled()) {
                response.sendRedirect("/notVerified");
                return false;
            }
        }

        return true;
    }
}