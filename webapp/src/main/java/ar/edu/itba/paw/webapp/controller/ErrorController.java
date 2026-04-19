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
        mav.addObject("errorMessage", "No tenés permisos para acceder a esta página.");
        mav.addObject("errorDescription", "Si creés que es un error, intentá iniciar sesión con otra cuenta.");
        return mav;
    }
}
