package com.example.tournament.exception;

/** Opération interdite par l'état courant du domaine → HTTP 422. */
public class InvalidOperationException extends BusinessException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
