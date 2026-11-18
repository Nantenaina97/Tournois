package com.example.tournament.exception;

/** Conflit avec l'état existant (doublon, unicité) → HTTP 409. */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
