package com.perpustakaan.exception;

import java.util.Map;

/**
 * Validation error dengan pemetaan nama field ke pesan.
 */
public class ValidationException extends AppException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String field, String message) {
        super(message);
        this.fieldErrors = Map.of(field, message);
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super(firstMessage(fieldErrors));
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    private static String firstMessage(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Data tidak valid.";
        }
        return errors.values().iterator().next();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public String getFieldError(String field) {
        return fieldErrors.get(field);
    }
}
