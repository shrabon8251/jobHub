package com.example.jobhub.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class MvcExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MvcExceptionHandler.class);

    @ExceptionHandler({
            DuplicateEmailException.class,
            DuplicateApplicationException.class,
            DuplicateSavedJobException.class,
            InactiveJobException.class,
            IllegalArgumentException.class
    })
    public String input(RuntimeException exception, RedirectAttributes flash) {
        flash.addFlashAttribute("error", friendlyMessage(exception));
        return "redirect:/";
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public String validation(BindException exception, RedirectAttributes flash) {
        flash.addFlashAttribute("error", firstValidationMessage(exception));
        return "redirect:/";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String constraintViolation(ConstraintViolationException exception, RedirectAttributes flash) {
        flash.addFlashAttribute("error", exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("Please review the submitted values."));
        return "redirect:/";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String uploadTooLarge(RedirectAttributes flash) {
        flash.addFlashAttribute("error", "Images must be 5 MB or smaller.");
        return "redirect:/";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String dataConflict(RedirectAttributes flash) {
        flash.addFlashAttribute("error", "That change conflicts with existing platform data.");
        return "redirect:/";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalidParameter(Model model) {
        model.addAttribute("message", "One of the values in that request was not valid.");
        return "error/not-found";
    }

    @ExceptionHandler({ForbiddenOwnershipException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden() {
        return "error/access-denied";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(Model model) {
        model.addAttribute("message", "The requested resource is unavailable.");
        return "error/not-found";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String missingRoute(Model model) {
        model.addAttribute("message", "The page you requested is unavailable.");
        return "error/not-found";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String unexpected(Exception exception) {
        log.error("Unhandled MVC exception", exception);
        return "error/server-error";
    }

    private String friendlyMessage(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "We could not complete that request. Please review the form and try again."
                : exception.getMessage();
    }

    private String firstValidationMessage(BindException exception) {
        return exception.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(message -> message != null && !message.isBlank())
                .findFirst()
                .orElse("Please review the submitted values.");
    }
}
