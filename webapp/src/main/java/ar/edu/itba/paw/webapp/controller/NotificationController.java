package ar.edu.itba.paw.webapp.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.services.NotificationService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RequestMapping(value = "/notifications/read", method = RequestMethod.POST)
    public ModelAndView markRead(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @RequestParam("id") final Long notificationId,
            final HttpServletRequest request) {
        notificationService.markRead(authUser.getUser().getId(), notificationId);
        return redirectToReferer(request);
    }

    @RequestMapping(value = "/notifications/read-all", method = RequestMethod.POST)
    public ModelAndView markAllRead(
            @AuthenticationPrincipal final PawAuthUser authUser,
            final HttpServletRequest request) {
        notificationService.markAllRead(authUser.getUser().getId());
        return redirectToReferer(request);
    }

    private ModelAndView redirectToReferer(final HttpServletRequest request) {
        final String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return new ModelAndView("redirect:" + referer);
        }
        return new ModelAndView("redirect:/");
    }
}
