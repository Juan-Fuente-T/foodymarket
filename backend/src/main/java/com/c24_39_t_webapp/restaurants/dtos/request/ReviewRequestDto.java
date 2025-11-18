package com.c24_39_t_webapp.restaurants.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record ReviewRequestDto(
        @NotNull(message = "El restaurante no puede estar vacío")
        Long restaurantId,
        @NotNull(message = "El puntaje no puede estar vacío")
        @Range(min = 0, max = 10)
        Integer score,
        @NotBlank(message = "El mensaje no puede estar vacío")
        String comments
) {
}
