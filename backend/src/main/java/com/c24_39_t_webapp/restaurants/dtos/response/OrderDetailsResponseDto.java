package com.c24_39_t_webapp.restaurants.dtos.response;

import java.math.BigDecimal;

public record OrderDetailsResponseDto(
        Long odt_id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal productPrice,
        Double subtotal
    ) {}