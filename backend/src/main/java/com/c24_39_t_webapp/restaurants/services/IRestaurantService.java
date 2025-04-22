package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IRestaurantService {
    RestaurantResponseDto registerRestaurant(Restaurant restaurant, String username);
    RestaurantResponseDto updateRestaurant(Restaurant restaurant);
    List<RestaurantResponseDto> findAll();
    RestaurantResponseDto findById(Long id);
    Restaurant findRestaurantEntityById(Long id);
//    List<RestaurantResponseDto> findRestaurantEntityByOwnerId(Long ownerId);
    List<RestaurantResponseDto> findRestaurantsByOwnerId(Long ownerId);
    void deleteById(Long id);
    Set<Category> getOfferedCategories(Long restaurantId);
    Category addCategoryToRestaurant(Long restaurantId, CategoryRequestDto categoryRequestDto);
}
