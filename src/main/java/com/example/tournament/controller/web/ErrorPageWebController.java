package com.example.tournament.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Cible interne des pages d'erreur personnalisées.
 *
 * <p>Ces pages sont atteintes par {@code forward} depuis la chaîne MVC
 * ({@code accessDeniedPage("/error/403")}, page d'erreur du conteneur, gestionnaire
 * d'exceptions). Un {@code forward} conserve la méthode HTTP de la requête
 * d'origine : un formulaire refusé pour cause de jeton CSRF invalide arrive donc
 * ici en {@code POST}. Les {@code viewController} de {@code WebConfig} n'acceptant
 * que GET et HEAD, ils renvoyaient alors {@code 405 Method Not Allowed} au lieu de
 * la page d'erreur. Les mappings ci-dessous n'imposent donc aucune méthode.</p>
 *
 * <p>{@code /error} reste réservé au {@code BasicErrorController} de Spring Boot.</p>
 */
@Controller
public class ErrorPageWebController {

    @RequestMapping("/error/403")
    public String forbidden() {
        return "error/403";
    }

    @RequestMapping("/error/404")
    public String notFound() {
        return "error/404";
    }

    @RequestMapping("/error/500")
    public String serverError() {
        return "error/500";
    }

    @RequestMapping("/error/business-error")
    public String businessError() {
        return "error/business-error";
    }
}
