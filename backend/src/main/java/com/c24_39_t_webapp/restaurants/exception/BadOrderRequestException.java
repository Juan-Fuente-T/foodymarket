package com.c24_39_t_webapp.restaurants.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando los datos de una orden son inválidos.
 * Por ejemplo: restaurantId <= 0, clientId <= 0, total no coincide con suma de detalles, etc.
 *
 * Hereda de ApplicationException y define HttpStatus.BAD_REQUEST (400)
 * Permite que GlobalExceptionHandler la maneje automáticamente.
 *
 * @see ApplicationException
 * @see GlobalExceptionHandler
 */
public class BadOrderRequestException extends ApplicationException {

    public BadOrderRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

//    public BadOrderRequestException(String message, Throwable cause) {
//        super(message, HttpStatus.BAD_REQUEST, cause);
//    }
}