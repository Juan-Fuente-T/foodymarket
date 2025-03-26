package com.c24_39_t_webapp.restaurants.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record RestaurantRequestDto(
        @NotBlank(message = "El nombre del restaurante no puede estar vacío.")
        String name,

        @NotBlank(message = "La descripción no puede estar vacía.")
        String description,

        @NotBlank(message = "La categoria no puede estar vacía.")
        String categoria,

        @NotBlank(message = "El teléfono no puede estar vacío.")
        String phone,

        @NotBlank(message = "La dirección no puede estar vacía.")
        String address,

        String logo
) {
}
