package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.models.Restaurant;

import java.util.List;

public interface IRestaurantService {
    RestaurantResponseDto registerRestaurant(Restaurant restaurant, String username);
    RestaurantResponseDto updateRestaurant(Restaurant restaurant);
    List<RestaurantResponseDto> findAll();
    RestaurantResponseDto findById(Long id);
    Restaurant findRestaurantEntityById(Long id);
    void deleteById(Long id);

}
