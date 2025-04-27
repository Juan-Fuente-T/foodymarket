package com.c24_39_t_webapp.restaurants.dtos.request;

import com.c24_39_t_webapp.restaurants.models.OrderStatus;

public record OrderUpdateRequestDto(
        OrderStatus status,
        String comments
) {}