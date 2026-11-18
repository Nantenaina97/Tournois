package com.example.tournament.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Redirige les réponses 404 du conteneur vers {@code /error}.
 *
 * <p>Spring Boot n'enregistre par défaut une page d'erreur conteneur que pour les statuts
 * 5xx. Sans ce registre, une URL inconnue (par exemple une ressource statique absente)
 * produit une réponse 404 sans corps : les pages d'erreur Thymeleaf {@code error/404} ne
 * sont alors jamais rendues.</p>
 *
 * <p>Une fois redirigées vers {@code /error}, le {@code BasicErrorController} résout la
 * vue {@code error/404} et conserve le statut HTTP 404.</p>
 */
@Component
public class NotFoundErrorPageRegistrar implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        registry.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error"));
    }
}
