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
import ar.edu.itba.paw.services.PurchaseExpiredException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFound(final ResourceNotFoundException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "404");
        mav.addObject("errorMessageCode", "Error.404.message.resourceNotFound");
        mav.addObject("errorDescriptionCode", "Error.404.description.resourceNotFound");
        return mav;
    }

    @ExceptionHandler(PurchaseExpiredException.class)
    public ModelAndView handlePurchaseExpired(final PurchaseExpiredException e) {
        return new ModelAndView("redirect:/purchases/" + e.getPurchaseId() + "?expired=1");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandlerFound(final NoHandlerFoundException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "404");
        mav.addObject("errorMessageCode", "Error.404.message.noHandler");
        mav.addObject("errorDescriptionCode", "Error.404.description.noHandler");
        return mav;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ModelAndView handleMethodNotAllowed(final HttpRequestMethodNotSupportedException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "405");
        mav.addObject("errorMessageCode", "Error.405.message");
        mav.addObject("errorDescriptionCode", "Error.405.description");
        return mav;
    }

    @ExceptionHandler({
        IllegalArgumentException.class, 
        IllegalStateException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.validation.BindException.class,
        org.springframework.beans.TypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequest(final Exception e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "400");
        mav.addObject("errorMessageCode", "Error.400.message.badRequest");
        mav.addObject("errorDescriptionCode", "Error.400.description.badRequest");
        return mav;
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleDataAccessException(final org.springframework.dao.DataAccessException e) {
        LOGGER.error("Database access error", e);
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "500");
        mav.addObject("errorMessageCode", "Error.500.message.dataAccess");
        mav.addObject("errorDescriptionCode", "Error.500.description.dataAccess");
        return mav;
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleInvalidUpload(final MultipartException e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "400");
        mav.addObject("errorMessageCode", "Error.400.message.invalidUpload");
        mav.addObject("errorDescriptionCode", "Error.400.description.invalidUpload");
        return mav;
    }

    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleForbidden(final Exception e) {
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "403");
        mav.addObject("errorMessageCode", "Error.403.message");
        mav.addObject("errorDescriptionCode", "Error.403.description");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(final Exception e) {
        LOGGER.error("Unhandled exception while processing request", e);
        final ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", "500");
        mav.addObject("errorMessageCode", "Error.500.message.generic");
        mav.addObject("errorDescriptionCode", "Error.500.description.generic");
        return mav;
    }
}
