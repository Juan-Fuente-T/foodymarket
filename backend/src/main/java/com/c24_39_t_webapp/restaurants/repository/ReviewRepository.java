package com.c24_39_t_webapp.restaurants.repository;

import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    //Carga la entidad Restaurant y UserEntity asociadas a la reseña para evitar N+1 y evitar problemas de rendimiento
    @EntityGraph(attributePaths = {"restaurant", "user"})
    List<Review> findByRestaurant(Restaurant restaurant);
}
