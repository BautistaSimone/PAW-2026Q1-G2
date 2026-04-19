package ar.edu.itba.paw.webapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.exception.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFound(final ResourceNotFoundException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "404");
        mav.addObject("errorMessage", "El recurso solicitado no fue encontrado.");
        mav.addObject("errorDescription", "Es posible que el contenido haya sido eliminado o que la dirección sea incorrecta.");
        return mav;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandlerFound(final NoHandlerFoundException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "404");
        mav.addObject("errorMessage", "La página que buscas no existe.");
        mav.addObject("errorDescription", "Revisá la dirección o volvé al inicio para seguir navegando.");
        return mav;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ModelAndView handleMethodNotAllowed(final HttpRequestMethodNotSupportedException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "405");
        mav.addObject("errorMessage", "Método HTTP no permitido.");
        mav.addObject("errorDescription", "La acción que intentaste no es válida para esta página.");
        return mav;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequest(final RuntimeException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "400");
        mav.addObject("errorMessage", "Petición inválida.");
        mav.addObject("errorDescription", "Los datos enviados no son correctos. Revisá e intentá nuevamente.");
        return mav;
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleInvalidUpload(final MultipartException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "400");
        mav.addObject("errorMessage", "Archivo invÃ¡lido.");
        mav.addObject("errorDescription", "El archivo enviado no cumple con los lÃ­mites permitidos.");
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleForbidden(final AccessDeniedException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "403");
        mav.addObject("errorMessage", "No tenés permisos para acceder a esta página.");
        mav.addObject("errorDescription", "Si creés que es un error, intentá iniciar sesión con otra cuenta.");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(final Exception e) {
        LOGGER.error("Unhandled exception while processing request", e);
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "500");
        mav.addObject("errorMessage", "Error interno del servidor.");
        mav.addObject("errorDescription", "Algo salió mal de nuestro lado. Por favor intentá nuevamente más tarde.");
        return mav;
    }
}
