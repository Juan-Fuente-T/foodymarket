package com.c24_39_t_webapp.restaurants.dtos.request;

import com.c24_39_t_webapp.restaurants.models.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDto(
    @NotNull(message = "El ID del pedido no puede ser nulo.")
    Long orderDetailsId,

    @NotNull(message = "El ID del cliente del pedido  no puede ser nulo.")
    Long clientId,

    @NotNull(message = "El ID del restaurante del pedido  no puede ser nulo.")
    Long restaurantId,
    @NotNull(message = "El estado del pedido  no puede ser nulo.")
    OrderStatus status,

    @NotNull(message = "El total del pedido  no puede ser nulo.")
    Double total,

    @Size(max = 500, message = "El tamaño de la categoria no puede exceder de 500 caracteres.")
    String comments,

    @NotNull(message = "Los detalles del pedido no pueden ser nulos.")
    @Size(min = 1, message = "Debe haber al menos un detalle en el pedido.")
    List<OrderDetailsRequestDto> details
    ) {}

