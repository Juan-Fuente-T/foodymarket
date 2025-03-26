package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.GroupedProductsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Product;
import com.c24_39_t_webapp.restaurants.models.Restaurant;

import java.util.List;
import java.util.Map;

public interface IProductService {
    ProductResponseDto addProduct(Product product);
    List<ProductResponseDto> findAllProducts();

    ProductResponseDto findProductById(Long prd_id);

    ProductResponseDto updateProduct(Product product);

    void deleteProduct(Long prd_id);

//    List<ProductSummaryResponseDto> findProductsByCategory(Long categoryId);
    List<ProductSummaryResponseDto> findProductsByCategory(Category category);

    List<ProductSummaryResponseDto> findProductsByName(String name);

    List<ProductResponseDto> findProductsByRestaurantId(Long restaurantId);
    List<GroupedProductsResponseDto> findProductsByRestaurantIdAndCategory(Long restaurantId);
}
