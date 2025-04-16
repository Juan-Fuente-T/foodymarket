package com.c24_39_t_webapp.restaurants.dtos.response;

public record RestaurantResponseDto(
        Long rst_id,
        Long rst_user_id,
        String name,
        String description,
        String category,
        String phone,
        String address,
        String logo
) {
}