package com.example.tournament.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Gestion centralisée des exceptions pour l'interface MVC Thymeleaf.
 *
 * <p>L'utilisateur reçoit une page d'erreur lisible contenant un message métier explicite
 * plutôt qu'une stack trace.</p>
 */
@ControllerAdvice(basePackages = "com.example.tournament.controller.web")
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ResourceNotFoundException ex, Model model) {
        return render("error/404", ex.getMessage(), HttpStatus.NOT_FOUND, model);
    }

    @ExceptionHandler({ConflictException.class, InvalidOperationException.class, BusinessException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ModelAndView handleBusiness(RuntimeException ex, Model model) {
        return render("error/business-error", ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, model);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception ex, Model model) {
        log.error("Erreur inattendue dans l'interface web", ex);
        return render("error/500", "Une erreur technique est survenue. Veuillez réessayer plus tard.",
                HttpStatus.INTERNAL_SERVER_ERROR, model);
    }

    private ModelAndView render(String view, String message, HttpStatus status, Model model) {
        model.addAttribute("message", message);
        model.addAttribute("status", status.value());
        model.addAttribute("errorLabel", status.getReasonPhrase());
        return new ModelAndView(view);
    }
}
