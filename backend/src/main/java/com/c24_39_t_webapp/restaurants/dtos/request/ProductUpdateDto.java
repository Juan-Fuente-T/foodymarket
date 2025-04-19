package com.c24_39_t_webapp.restaurants.dtos.request;

import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;

import java.math.BigDecimal;

public record ProductUpdateDto(
        Long restaurantId,
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        String image,
        Boolean isActive,
        Integer quantity,
        String categoryName

) {
}
