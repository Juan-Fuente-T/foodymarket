package com.c24_39_t_webapp.restaurants.exception;

/**
 * Excepción lanzada con un mensaje personalizado cuando no se encuentra un pedido específico.
 * Hereda de NotFoundException.
 * @see NotFoundException
 */
public class OrderNotFoundException extends NotFoundException {

        public OrderNotFoundException(String message) {
            super(message);
        }

        @Override
        public String getResourceName() {
            return "Pedido";
        }
    }