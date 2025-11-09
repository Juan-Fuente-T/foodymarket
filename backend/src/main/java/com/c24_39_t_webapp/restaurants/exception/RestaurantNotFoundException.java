package com.c24_39_t_webapp.restaurants.exception;

/**
 * Excepción lanzada con un mensaje personalizado cuando no se encuentra un restaurante específico.
 * Hereda de NotFoundException.
 * @see NotFoundException
 *
 */
public class RestaurantNotFoundException extends NotFoundException {
    public RestaurantNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getResourceName() {
        return "Restaurante";
    }
}