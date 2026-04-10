package ar.edu.itba.paw.webapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFound(final ResourceNotFoundException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "404");
        mav.addObject("errorMessage", "El recurso solicitado no fue encontrado.");
        return mav;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequest(final RuntimeException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "400");
        mav.addObject("errorMessage", "Petición inválida.");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(final Exception e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "500");
        mav.addObject("errorMessage", "Error interno del servidor.");
        return mav;
    }
}
