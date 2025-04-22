package com.c24_39_t_webapp.restaurants.repository;

import com.c24_39_t_webapp.restaurants.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    @Query(value = "SELECT COUNT(*) FROM categorias_restaurante cr WHERE cr.categoria_id = :categoryId", nativeQuery = true)
    long countRestaurantsUsingCategory(Long categoryId);
}
