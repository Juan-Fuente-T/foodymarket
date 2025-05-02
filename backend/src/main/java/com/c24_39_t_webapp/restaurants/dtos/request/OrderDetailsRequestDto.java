package com.c24_39_t_webapp.restaurants.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderDetailsRequestDto(
        @NotNull(message = "El ID del producto no puede ser nulo.")
        Long productId,

        @NotNull(message = "La cantidad no puede ser nula.")
        @Min(value = 1, message = "La cantidad debe ser al menos 1.")
        Integer quantity,

        @NotNull(message = "La cantidad del subtotal no puede ser nula.")
        @Min(value = 0, message = "La cantidad debe mayor que cero.")
        BigDecimal subtotal
) {}
