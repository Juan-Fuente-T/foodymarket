package com.c24_39_t_webapp.restaurants.dtos.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;


public record UpdateReviewDto(
        @NotBlank
        Long reviewToUpdateId,
        @NotBlank
        @Range(min = 0, max = 10)
        Integer score,
        @NotBlank
        String comments
) {
}
