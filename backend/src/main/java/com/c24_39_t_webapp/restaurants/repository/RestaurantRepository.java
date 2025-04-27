package com.c24_39_t_webapp.restaurants.repository;

import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByIdAndUserEntityEmail(Long id, String email);

    // Este método ahora devuelve DIRECTAMENTE la lista de DTOs.
    @Query("SELECT new com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto(" +
            "r.id, ue.id, r.name, r.description, r.phone, r.email, r.address, r.openingHours, r.logo, r.coverImage, " +
            "rc.id, rc.name) " +
            "FROM Restaurant r JOIN r.userEntity ue JOIN r.cuisine rc "  +
            "WHERE ue.id = :ownerUserId")
    List<RestaurantResponseDto> findRestaurantsByOwnerId(@Param("ownerUserId") Long ownerUserId);

    // Este devuelve solo los IDs de los restaurantes de un dueño
    @Query("SELECT r.id FROM Restaurant r WHERE r.userEntity.id = :ownerId")
    List<Long> findRestaurantIdsByOwnerId(@Param("ownerId") Long ownerId);

    //Estos dan problemas de rendimiento, necesitan que el Dto haga una llamada a la base de datos.
    //List<Restaurant> findByUserEntityId(Long ownerId);
    //@Query("SELECT r FROM Restaurant r JOIN FETCH r.userEntity WHERE r.userEntity.id = :ownerId")
    //List<Restaurant> findRestaurantsByOwnerId(@Param("ownerId") Long ownerId);
}
