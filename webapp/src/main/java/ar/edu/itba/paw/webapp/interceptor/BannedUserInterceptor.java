package ar.edu.itba.paw.webapp.interceptor;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;

@Component
public class BannedUserInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Autowired
    public BannedUserInterceptor(@Lazy final UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof PawAuthUser) {
            PawAuthUser authUser = (PawAuthUser) auth.getPrincipal();
            Long userId = authUser.getUser().getId();

            Optional<User> freshUser = userService.findById(userId);
            if (freshUser.isPresent() && Boolean.TRUE.equals(freshUser.get().getBanned())) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();
                response.sendRedirect(request.getContextPath() + "/banned");
                return false;
            }
        }

        return true;
    }
}
