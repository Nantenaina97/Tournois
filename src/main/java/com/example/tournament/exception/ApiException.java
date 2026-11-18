package com.example.tournament.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Erreur REST levée directement par Spring (sécurité, Spring Data). */
public class ApiException extends ResponseStatusException {

    public ApiException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
