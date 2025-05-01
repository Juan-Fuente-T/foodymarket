package com.c24_39_t_webapp.restaurants.repository;

import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Aquí se pueden agregar métodos de consulta personalizados si es necesario
//    @Query("SELECT p FROM Product p WHERE p.category.ctg_id = :categoryId")
//    List<Product> findProductsByCategory(@Param("categoryId") Long categoryId);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"restaurant", "category"})
    List<Product> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"restaurant", "category"})
    Optional<Product> findById(@NonNull Long prd_id);

    @EntityGraph(attributePaths = {"restaurant", "category"})
    List<Product> findProductsByCategoryId(Long categoryId);

    //    @Query("SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:name)")

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    @EntityGraph(attributePaths = {"restaurant", "category"})
    List<Product> findProductsByName(@Param("name") String name);

    //    @Query("SELECT p FROM Product p WHERE p.restaurant.rst_id = :restaurantId")
    @EntityGraph(attributePaths = {"category", "restaurant"})
    // Carga category y restaurant
    List<Product> findProductsByRestaurantId(Long restaurantId);

    boolean existsByCategoryId(Long categoryId);

//    @Query("SELECT p FROM Product p WHERE p.restaurant = :restaurant")
//    List<GroupedProductsResponseDto> findProductsByRestaurantAndCategory(@Param("restaurant") Restaurant restaurant);

//    @Query(value = "SELECT p FROM Product p WHERE p.restaurant.id = :restaurantId ORDER BY p.category.name", nativeQuery = true)

    // Este método devuelve DIRECTAMENTE List<ProductResponseDto>
    @Query("SELECT new com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto(" +
            "p.prd_id, p.restaurant.id, p.category.id, p.name, p.description, " +
            "p.price, p.image, p.isActive, p.quantity, " +
            "c.name, r.name)" +
            "FROM Product p JOIN p.category c JOIN p.restaurant r " +
            "WHERE r.id = :restaurantId " +
            "ORDER BY c.name, p.name")
    List<ProductResponseDto> findProductsByRestaurantIdAndCategory(@Param("restaurantId") Long restaurantId);
//    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.restaurant.id = :restaurantId ORDER BY p.category.name, p.name")
//    List<Product> findProductsByRestaurantIdAndCategory(@Param("restaurantId") Long restaurantId);
}
