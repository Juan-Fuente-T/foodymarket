package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.ProductUpdateDto;
import com.c24_39_t_webapp.restaurants.dtos.response.GroupedProductsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;

import java.util.List;

public interface IProductService {
    ProductResponseDto addProduct(ProductRequestDto productRequestDto);
    List<ProductResponseDto> findAllProducts();

    ProductResponseDto findProductById(Long prd_id);

    ProductResponseDto updateProduct(Long productId, ProductUpdateDto updateDto);

    void deleteProduct(Long prd_id);

//    List<ProductSummaryResponseDto> findProductsByCategory(Long categoryId);
    List<ProductSummaryResponseDto> findProductsByCategoryId(Long categoryId);
    List<ProductSummaryResponseDto> findProductsByName(String name);

    List<ProductResponseDto> findProductsByRestaurantId(Long restaurantId);
    List<GroupedProductsResponseDto> findProductsByRestaurantIdAndCategory(Long restaurantId);
}
