package com.c24_39_t_webapp.restaurants.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestaurantRequestDto(
        @NotNull(message = "El id del propietario no puede estar vacío.")
        Long ownerId,
        @NotBlank(message = "El nombre del restaurante no puede estar vacío.")
        String name,

        @NotBlank(message = "La descripción no puede estar vacía.")
        String description,

        @NotNull(message = "El tipo de cocina no puede estar vacío.")
        Long cuisineId,

        @NotBlank(message = "El teléfono no puede estar vacío.")
        String phone,

        @NotBlank(message = "El email no puede estar vacío.")
        String email,

        @NotBlank(message = "La dirección no puede estar vacía.")
        String address,

        String openingHours,

        String logo,

        String coverImage
) {
}
