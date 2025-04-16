package com.c24_39_t_webapp.restaurants.repository;

import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Aquí se pueden agregar métodos de consulta personalizados si es necesario
//    @Query("SELECT p FROM Product p WHERE p.category.ctg_id = :categoryId")
//    List<Product> findProductsByCategory(@Param("categoryId") Long categoryId);

    List<Product> findProductsByCategory(Category category);

    //    @Query("SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:name)")

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findProductsByName(@Param("name") String name);

//    @Query("SELECT p FROM Product p WHERE p.restaurant.rst_id = :restaurantId")
    List<Product> findProductsByRestaurantId(Long restaurantId);

//    @Query("SELECT p FROM Product p WHERE p.restaurant = :restaurant")
//    List<GroupedProductsResponseDto> findProductsByRestaurantAndCategory(@Param("restaurant") Restaurant restaurant);

//    @Query(value = "SELECT p FROM Product p WHERE p.restaurant.id = :restaurantId ORDER BY p.category.name", nativeQuery = true)
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.restaurant.id = :restaurantId ORDER BY p.category.name, p.name")
    List<Product> findProductsByRestaurantIdAndCategory(@Param("restaurantId") Long restaurantId);
}
