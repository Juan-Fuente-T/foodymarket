package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.AddReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;

import java.util.List;

public interface IReviewService {

    ReviewResponseDto addReview(AddReviewDto reviewDto, UserDetailsImpl userDetails);

    List<ReviewResponseDto> getAllRestaurantReviews(Long restaurantId);

    ReviewResponseDto getReviewById(Long id);

    ReviewResponseDto updateReview(UpdateReviewDto updateReviewDto, UserDetailsImpl userDetails);

    void deleteReview(Long id, UserDetailsImpl userDetails);
}
