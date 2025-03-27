package com.c24_39_t_webapp.restaurants.dtos.response;

import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.Review;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ReviewResponseDto {

    private Long id;
    private Restaurant restaurant;
    private UserEntity userEntity;
    private Integer score;
    private String comments;
    private LocalDateTime createdAt;

    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.restaurant = review.getRestaurant();
        this.userEntity = review.getUserEntity();
        this.score = review.getScore();
        this.comments = review.getComments();
        this.createdAt = getCreatedAt();
    }
}
