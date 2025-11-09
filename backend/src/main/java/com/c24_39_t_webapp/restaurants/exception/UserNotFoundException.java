package com.c24_39_t_webapp.restaurants.exception;

/**
 * Excepción lanzada con un mensaje personalizado cuando no se encuentra un usuario específico.
 * Hereda de NotFoundException.
 * @see NotFoundException
 *
 */
public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String id) {
        super("Usuario con ID " + id + " no encontrado");
    }

    @Override
    public String getResourceName() {
        return "Usuario";
    }
}