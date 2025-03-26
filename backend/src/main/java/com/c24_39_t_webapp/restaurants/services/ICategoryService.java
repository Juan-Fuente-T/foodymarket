package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;

import java.util.List;

public interface ICategoryService {
    CategoryResponseDto addCategory(Category category);

    List<CategoryResponseDto> findAllCategories();

    CategoryResponseDto findCategoryById(Long id);

    CategoryResponseDto updateCategory(Category category);

    void deleteCategory(Long id);
}