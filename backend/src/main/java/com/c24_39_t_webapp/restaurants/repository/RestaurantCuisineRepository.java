package com.c24_39_t_webapp.restaurants.repository;

import com.c24_39_t_webapp.restaurants.models.RestaurantCuisine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantCuisineRepository extends JpaRepository<RestaurantCuisine, Long> {
    // Aquí se pueden agregar métodos personalizados si es necesario
    // Por ejemplo, para buscar por nombre de cocina:
//    RestaurantCuisine findByName(String name);
}