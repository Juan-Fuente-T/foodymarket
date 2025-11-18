package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.ReviewRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;

import java.util.List;

public interface IReviewService {

    ReviewResponseDto addReview(ReviewRequestDto reviewDto, Long reviewId);

    List<ReviewResponseDto> getAllRestaurantReviews(Long restaurantId);

    ReviewResponseDto getReviewById(Long id);

    ReviewResponseDto updateReview(UpdateReviewDto updateReviewDto, Long reviewId, Long userId);

    void deleteReview(Long reviewId, Long userId);
}
