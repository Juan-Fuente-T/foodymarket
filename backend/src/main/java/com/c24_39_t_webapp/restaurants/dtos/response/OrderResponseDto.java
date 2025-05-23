package com.c24_39_t_webapp.restaurants.dtos.response;

import com.c24_39_t_webapp.restaurants.models.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
    Long ord_Id,
    Long clientId,
    Long restaurantId,
    String restaurantName,
    OrderStatus status,
    BigDecimal total,
    String comments,
    List<OrderDetailsResponseDto> details,
    String createdAt,
    String updatedAt
) {
}
