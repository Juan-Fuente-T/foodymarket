package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.AddReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;

import java.util.List;

public interface IReviewService {

    ReviewResponseDto addReview(AddReviewDto reviewDto, Long reviewId);

    List<ReviewResponseDto> getAllRestaurantReviews(Long restaurantId);

    ReviewResponseDto getReviewById(Long id);

    ReviewResponseDto updateReview(UpdateReviewDto updateReviewDto, Long reviewId);

    void deleteReview(Long reviewId, Long userId);
}
