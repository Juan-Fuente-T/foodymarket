package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;

import java.util.List;

public interface IProductService {
    ProductResponseDto addProduct(ProductRequestDto productRequestDto, String username, Long restaurantId);

    List<ProductResponseDto> findAllProducts();

    ProductResponseDto findProductById(Long prd_id);

    ProductResponseDto updateProduct(Long prd_id, ProductRequestDto updateDto);
//
//    ProductResponseDto deleteProduct(Long prd_id);

}
