package com.c24_39_t_webapp.restaurants.dtos.request;

import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequestDto(
        @NotNull(message = "El ID del restaurante no puede ser nulo.")
        Long restaurantId,

        @NotNull(message = "La categoria del producto no puede ser nulo.")
        Long categoryId,
        @NotBlank(message = "El nombre del producto no puede estar vacío.")
        @Size(max = 60, message = "El nombre del producto no puede tener más de 40 caracteres.")
        String name,

        @NotBlank(message = "La descripción no puede estar vacía.")
        @Size(max = 500, message = "El tamaño de la categoria no puede exceder de 500 caracteres.")
        String description,

        @NotNull(message = "El precio no puede estar ser nulo.")
        BigDecimal price,

        @NotBlank(message = "La imagen no puede estar vacía.")
        String image,

        @NotNull(message = "El estado no puede ser nulo.")
        Boolean isActive,

        @NotNull(message = "La cantidad no puede estar vacío.")
        Integer quantity

) {
}



