package com.antecsis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Respuesta unificada de error para la API.
 * Incluye success (siempre false), message (mensaje general) y/o errors (errores de validación por campo).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    boolean success,
    String message,
    Map<String, String> errors
) {
    public static ErrorResponse withMessage(String message) {
        return new ErrorResponse(false, message, null);
    }

    public static ErrorResponse withErrors(Map<String, String> errors) {
        return new ErrorResponse(false, null, errors);
    }
}
