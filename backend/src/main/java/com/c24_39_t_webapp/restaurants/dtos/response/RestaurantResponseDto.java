package com.c24_39_t_webapp.restaurants.dtos.response;

import com.c24_39_t_webapp.restaurants.models.Restaurant;

public record RestaurantResponseDto(
        Long rst_id,
        String name,
        String description,
        String categoria,
        String phone,
        String address,
        String logo
) {
}