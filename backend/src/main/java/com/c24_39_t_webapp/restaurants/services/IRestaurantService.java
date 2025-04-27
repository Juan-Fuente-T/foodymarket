package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRestaurantService {
    RestaurantResponseDto registerRestaurant(RestaurantRequestDto requestDto, String username);
    RestaurantResponseDto updateRestaurant(RestaurantRequestDto requestDto, Long rst_id);
    List<RestaurantResponseDto> findAll();
    RestaurantResponseDto findById(Long id);
//    Restaurant findRestaurantEntityById(Long id);
//    List<RestaurantResponseDto> findRestaurantEntityByOwnerId(Long ownerId);
    List<RestaurantResponseDto> findRestaurantsByOwnerId(Long ownerId);
    void deleteById(Long id);
    Set<CategoryResponseDto> getOfferedCategories(Long restaurantId);
    CategoryResponseDto addCategoryToRestaurant(Long restaurantId, CategoryRequestDto categoryRequestDto);

}
