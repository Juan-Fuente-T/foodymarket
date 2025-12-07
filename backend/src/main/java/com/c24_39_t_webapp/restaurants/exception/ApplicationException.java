// Archivo: src/main/java/com/c24_39_t_webapp/restaurants/exception/ApplicationException.java

package com.c24_39_t_webapp.restaurants.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para TODA la aplicación.
 *
 * Todas las excepciones de negocio deben heredar de esta clase.
 * Cada una define su propio HttpStatus, permitiendo que GlobalExceptionHandler
 * las maneje de forma uniforme sin importar el tipo específico.
 *
 * FUNDAMENTO: Spring Best Practices + Google Java Style Guide
 */
public abstract class ApplicationException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ApplicationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

//    public ApplicationException(String message, HttpStatus httpStatus, Throwable cause) {
//        super(message, cause);
//        this.httpStatus = httpStatus;
//    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}