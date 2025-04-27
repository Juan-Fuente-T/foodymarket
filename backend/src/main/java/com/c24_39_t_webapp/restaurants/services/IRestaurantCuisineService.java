package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.response.CuisineResponseDto;

import java.util.List;

public interface IRestaurantCuisineService {
    List<CuisineResponseDto> findAll();
    // Se podrían añadir métodos para crear/editar/borrar si fueran necesarios (para un Admin)
}
