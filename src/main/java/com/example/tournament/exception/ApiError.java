package com.example.tournament.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

/** Corps d'erreur uniforme renvoyé par l'API REST. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private Instant timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private Map<String, String> errors;

    public ApiError() {
    }

    public static ApiError of(HttpStatus status, String code, String message, String path,
                              Map<String, String> errors) {
        ApiError error = new ApiError();
        error.timestamp = Instant.now();
        error.status = status.value();
        error.error = status.getReasonPhrase();
        error.code = code;
        error.message = message;
        error.path = path;
        error.errors = (errors == null || errors.isEmpty()) ? null : errors;
        return error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
