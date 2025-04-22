package com.c24_39_t_webapp.restaurants.dtos.response;

import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public record CategoryResponseDto(
        Long ctg_id,
        String name,
        String description
) {

}