package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.OrderRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderUpdateRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {
    OrderResponseDto addOrder(OrderRequestDto orderRequestDto, String username);

    List<OrderResponseDto> findAllOrders(Long restaurantId);

    List<OrderResponseDto> findAllOrdersByOwnerId(Long ownerId);

    Page<OrderResponseDto> findOrdersByOwnerIdPaged(Long ownerId, Pageable pageable);

    OrderResponseDto findOrderById(Long ord_id);

    OrderResponseDto updateOrder(Long ord_id, OrderUpdateRequestDto updateOrderDto);

    void deleteOrder(Long ord_id);

    List<OrderResponseDto> findByCreatedAtBetween(Long restaurantId, LocalDateTime start, LocalDateTime end);

    List<OrderResponseDto> findByClientId(Long clientId);

    List<OrderResponseDto> findByClientIdAndCreatedAtBetween(Long clientId, LocalDateTime start, LocalDateTime end);

    List<OrderResponseDto> findByStatusAndRestaurantId(OrderStatus status, Long restaurantId);

}
