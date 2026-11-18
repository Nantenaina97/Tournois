package com.example.tournament.exception;

/** Ressource inexistante → HTTP 404. */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String type, Object id) {
        return new ResourceNotFoundException(type + " introuvable : " + id);
    }
}
