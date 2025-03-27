package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.request.OrderRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderUpdateRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.models.OrderState;
import com.c24_39_t_webapp.restaurants.repository.OrderRepository;
import com.c24_39_t_webapp.restaurants.services.IOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/order")
@AllArgsConstructor
public class OrderController {

    private IOrderService orderService;
    private OrderRepository orderRepository;

    /**
     * Endpoint to add a new {@link ResponseEntity} Order object to the system.
     * Delegates the addition logic to {@link IOrderService#addOrder(OrderRequestDto, String)}.
     *
     * @param requestDto The {@code OrderRequestDto} object to add.
     * @param email      The email of the user adding the order.
     * @return The {@code OrderResponseDto} object representing the added order.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrderResponseDto> addOrder(@RequestBody OrderRequestDto requestDto, @RequestParam String email) {
        log.info("Recibida solicitud para añadir un pedido con los siguientes datos: {}", requestDto);
        OrderResponseDto responseDto = orderService.addOrder(requestDto, email);
        log.info("Pedido agregado exitosamente con los siguientes datos: {}", responseDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Endpoint to retrieve all {@link ResponseEntity} Order objects from the system.
     * Delegates the retrieval logic to {@link IOrderService#findAllOrders(Long)}.
     *
     * @return A list of {@code OrderResponseDto} objects representing all orders in the system.
     */
    @GetMapping
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<List<OrderResponseDto>> findAllOrders(Long restaurantId) {
        log.info("Solicitud recibida para obtener todos los pedidos.");
        List<OrderResponseDto> orders = orderService.findAllOrders(restaurantId);
        log.info("Se recuperaron {} pedidos exitosamente.", orders.size());
        return ResponseEntity.ok(orders);
    }

    /**
     * Endpoint to retrieve a single {@link ResponseEntity} Order object from the system.
     * Delegates the retrieval logic to {@link IOrderService#findOrderById(Long)}.
     *
     * @param ord_id The ID of the order to retrieve.
     * @return The {@code OrderResponseDto} object representing the retrieved order.
     */
    @GetMapping(value = "/{ord_id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<OrderResponseDto> findOrderById(@PathVariable Long ord_id) {
        log.info("Solicitud recibida para obtener el pedido con id: {}", ord_id);
        OrderResponseDto order = orderService.findOrderById(ord_id);
        log.info("Pedido recuperado exitosamente con los siguientes datos: {}", order);
        return ResponseEntity.ok(order);
    }

    /**
     * Endpoint to update an existing {@link ResponseEntity} Order object in the system.
     * Delegates the update logic to {@link IOrderService#updateOrder(Long, OrderUpdateRequestDto)}.
     *
     * @param ord_id          The ID of the order to update.
     * @param updateRequestDto The {@code OrderUpdateRequestDto} object containing the updated order details.
     * @return The {@code OrderResponseDto} object representing the updated order.
     */
    @PatchMapping(value = "/{ord_id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<OrderResponseDto> updateOrder(@PathVariable Long ord_id, @RequestBody OrderUpdateRequestDto updateRequestDto) {
        log.info("Solicitud recibida para actualizar el pedido con id: {} con los siguientes datos: {}", ord_id, updateRequestDto);
        OrderResponseDto order = orderService.updateOrder(ord_id, updateRequestDto);
        log.info("Pedido actualizado exitosamente con los siguientes datos: {}", order);
        return ResponseEntity.ok(order);
    }

    /**
     * Endpoint to delete an existing {@link ResponseEntity} Order object in the system.
     * Delegates the delete logic to {@link IOrderService#deleteOrder(Long)}.
     *
     * @param ord_id The ID of the order to delete.
     */
    @DeleteMapping(value = "/{ord_id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long ord_id) {
        log.info("Solicitud recibida para eliminar el pedido con id: {} junto a sus detalles de pedido", ord_id);
        orderService.deleteOrder(ord_id); // Llama al servicio
        log.info("Pedido con id {} eliminado exitosamente junto a los detalles de pedido.", ord_id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Endpoint to retrieve all {@link ResponseEntity} Order objects from the system within a specified date range.
     * Delegates the retrieval logic to {@link IOrderService#findByCreatedAtBetween(Long, LocalDateTime, LocalDateTime)}.
     *
     * @param start The start date of the range.
     * @param end   The end date of the range.
     * @return A list of {@code OrderResponseDto} objects representing all orders within the specified date range.
     */
    @GetMapping(value = "/byDate")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public List<OrderResponseDto> getOrdersByDate(
            @RequestParam Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("Solicitud recibida para obtener todos los pedidos realizados en la fecha {} y la fecha {}", start, end);
        List<OrderResponseDto> byDateOrders= orderService.findByCreatedAtBetween(restaurantId, start, end);
        log.info("Se recuperaron {} pedidos exitosamente.", byDateOrders.size());
        return byDateOrders;

    }
    /**
     * Endpoint to retrieve all {@link ResponseEntity} Order objects from the system by client ID.
     * Delegates the retrieval logic to {@link IOrderService#findByClientId(Long)}.
     *
     * @param cln_id The ID of the client to retrieve orders for.
     * @return A list of {@code OrderResponseDto} objects representing all orders for the specified client.
     */
    @GetMapping(value = "/byClientId/{cln_id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<OrderResponseDto> getOrdersByClientId(@PathVariable Long cln_id) {
        log.info("Solicitud recibida para obtener todos los pedidos realizados por el cliente con id: {}", cln_id);
        List<OrderResponseDto> clientOrders = orderService.findByClientId(cln_id);
        log.info("Se recuperaron {} pedidos exitosamente para el cliente {}.", clientOrders.size(), cln_id);
        return clientOrders;
    }

    /**
     * Endpoint to retrieve all {@link ResponseEntity} Order objects from the system by client ID and date range.
     * Delegates the retrieval logic to {@link IOrderService#findByClientIdAndCreatedAtBetween(Long, LocalDateTime, LocalDateTime)}.
     *
     * @param clientId The ID of the client to retrieve orders for.
     * @param start    The start date of the range.
     * @param end      The end date of the range.
     * @return A list of {@code OrderResponseDto} objects representing all orders for the specified client within the specified date range.
     */
    @GetMapping(value = "/byClientDate")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<OrderResponseDto> findByClientIdAndCreatedAtBetween(
            @RequestParam Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("Solicitud recibida para obtener todos los pedidos realizados por el cliente con id: {} entre la fecha {} y la fecha {}", clientId, start, end);
        List<OrderResponseDto> byClientDateOrders = orderService.findByClientIdAndCreatedAtBetween(clientId, start, end);
        log.info("Se recuperaron {} pedidos exitosamente.", byClientDateOrders.size());
        return byClientDateOrders;
    }
    /**
     * Endpoint to retrieve all {@link ResponseEntity} Order objects from the system by state.
     * Delegates the retrieval logic to {@link IOrderService#findByStateAndRestaurantId(OrderState, Long)}.
     *
     * @param state The state of the orders to retrieve.
     * @return A list of {@code OrderResponseDto} objects representing all orders in the specified state.
     */
    @GetMapping(value = "/byRestaurantAndState")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public List<OrderResponseDto> findByRestaurantIdAndState(
            @RequestParam OrderState state,
            @RequestParam Long restaurantId) {
        log.info("Solicitud recibida para obtener todos los pedidos realizados en el restaurante con id: {} en estado {}", restaurantId, state);
        List<OrderResponseDto> byRestaurantStateOrders = orderService.findByStateAndRestaurantId(state, restaurantId);
        log.info("Se recuperaron {} pedidos exitosamente.", byRestaurantStateOrders.size());
        return byRestaurantStateOrders;
    }
}
