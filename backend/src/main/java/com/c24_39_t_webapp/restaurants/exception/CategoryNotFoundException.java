package com.c24_39_t_webapp.restaurants.exception;

/**
 * Excepción lanzada con un mensaje personalizado cuando no se encuentra una categoría específica.
 * Hereda de NotFoundException.
 * @see NotFoundException
 */
public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getResourceName() {
        return "Categoria";
    }
}