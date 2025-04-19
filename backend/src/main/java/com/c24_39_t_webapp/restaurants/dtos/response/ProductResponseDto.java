package com.c24_39_t_webapp.restaurants.dtos.response;

import java.math.BigDecimal;

public record ProductResponseDto(
        Long prd_id,
        Long restaurantId,
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        String image,
        Boolean isActive,
        Integer quantity,
        String categoryName,
        String restaurantName
) {
}
