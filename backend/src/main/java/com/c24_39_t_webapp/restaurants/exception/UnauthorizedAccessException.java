package com.c24_39_t_webapp.restaurants.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción para acceso no autorizado (403 Forbidden).
 * Se lanza cuando el usuario está autenticado pero NO tiene permisos.
 */
public class UnauthorizedAccessException extends ApplicationException {

    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}