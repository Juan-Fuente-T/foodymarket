package com.c24_39_t_webapp.restaurants.exception;

/**
 * Excepción lanzada con un mensaje personalizado cuando no se encuentra un producto específico.
 * Hereda de NotFoundException.
 * @see NotFoundException
 *
 */
public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }

//    @Override
//    public String getResourceName() {
//        return "Producto";
//    }
}
