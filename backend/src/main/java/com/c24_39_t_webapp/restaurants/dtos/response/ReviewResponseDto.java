package com.c24_39_t_webapp.restaurants.dtos.response;

import java.time.LocalDateTime;

public record ReviewResponseDto(
//        Long id,
        Long restaurantId,
        Long userId,
        String userName,
        Integer score,
        String comments,
        LocalDateTime createdAt
) {
}
