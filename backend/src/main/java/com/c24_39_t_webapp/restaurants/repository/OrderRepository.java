package com.c24_39_t_webapp.restaurants.repository;

import com.c24_39_t_webapp.restaurants.models.Order;
import com.c24_39_t_webapp.restaurants.models.OrderStatus;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.clientId c LEFT JOIN FETCH o.details d LEFT JOIN FETCH d.product p WHERE o.restaurantId = :restaurant") // <-- Filtra por objeto restaurante
    List<Order> findByRestaurantId(
            @Param("restaurant")Restaurant restaurant
    );

    // Usando LEFT JOIN FETCH es más seguro si details puede estar vacío
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.clientId c LEFT JOIN FETCH o.details d LEFT JOIN FETCH d.product p WHERE o.id = :orderId")
    Optional<Order> findByIdFetchingAll(
            @Param("ord_id")long ord_id
    );
    @Query("SELECT o FROM Order o JOIN FETCH o.restaurantId r WHERE o.createdAt BETWEEN :start AND :end AND o.restaurantId.id = :restaurantId")
    List<Order> findByRestaurantIdAndCreatedAtBetween(
            @Param("restaurant")Restaurant restaurant,
            @Param("start")LocalDateTime start,
            @Param("end")LocalDateTime end
    );

    @Query("SELECT o FROM Order o JOIN FETCH o.clientId c WHERE o.clientId.id = :clientId")     List<Order> findByClientId_Id(
            @Param("clientId") Long clientId
    );

//    List<Order> findByClientId_IdAndCreatedAtBetween(Long clientId, LocalDateTime start, LocalDateTime end);
@Query("SELECT o FROM Order o JOIN FETCH o.restaurantId r WHERE o.createdAt BETWEEN :start AND :end AND o.restaurantId = :restaurant") // <-- Compara objeto
 List<Order> findByClientId_IdAndCreatedAtBetween(
            @Param("clientId") Long clientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    @Query("SELECT o FROM Order o JOIN FETCH o.clientId c JOIN FETCH o.details d JOIN FETCH d.product p WHERE o.status = :status AND o.restaurantId.id = :restaurantId")
//    o @EntityGraph(attributePaths = {"clientId", "details", "details.product"})
    List<Order> findByStatusAndRestaurantId_Id(
            @Param("status") OrderStatus status,
            @Param("restaurantId") Long restaurantId
    );

    // NUEVO: Busca órdenes por lista de IDs de restaurante, trayendo detalles y productos
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.details od LEFT JOIN FETCH od.product WHERE o.restaurantId.id IN :restaurantIds")
    List<Order> findByRestaurantId_IdInWithDetails(@Param("restaurantIds") List<Long> restaurantIds);
    // Nota: LEFT JOIN FETCH para que traiga órdenes aunque no tengan detalles/productos. DISTINCT para evitar duplicados por los joins.
}