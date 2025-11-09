package com.c24_39_t_webapp.restaurants.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción para recursos no encontrados (404 Not Found).
 * Clase abstracta que obliga a las subclases a definir qué recurso no fue encontrado.
 */
public abstract class NotFoundException extends ApplicationException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public abstract String getResourceName();
}