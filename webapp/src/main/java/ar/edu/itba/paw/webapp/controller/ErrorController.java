package ar.edu.itba.paw.webapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {

    @RequestMapping("/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView forbidden() {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "403");
        mav.addObject("errorMessageCode", "Error.403.message");
        mav.addObject("errorDescriptionCode", "Error.403.description");
        return mav;
    }

    @RequestMapping("/banned")
    public ModelAndView banned() {
        return new ModelAndView("banned");
    }
}
