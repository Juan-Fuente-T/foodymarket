package com.c24_39_t_webapp.restaurants.exception;

//package com.c24_39_t_webapp.restaurants.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción genérica para cualquier recurso no encontrado.
 * Alternativa a crear NotFoundException específicas.
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}