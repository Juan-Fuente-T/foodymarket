package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.OrderDetailsRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderUpdateRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderDetailsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.exception.*;
import com.c24_39_t_webapp.restaurants.models.*;
import com.c24_39_t_webapp.restaurants.repository.*;
import com.c24_39_t_webapp.restaurants.services.IOrderService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;
    private final OrderDetailsRepository orderDetailsRepository;


    @Override
    @Transactional
    public OrderResponseDto addOrder(OrderRequestDto orderRequestDto, String email) {
        log.info("Intentando crear un pedido para el usuario con email: {}", email);

        // Validaciones básicas
        validateOrderRequest(orderRequestDto);

        // Buscar entidades relacionadas
        Restaurant restaurant = restaurantRepository.findById(orderRequestDto.restaurantId()).orElseThrow(() -> new RestaurantNotFoundException("No se ha encontrado el restaurante"));
        UserEntity client = userRepository.findById(orderRequestDto.clientId()).orElseThrow(() -> new UserNotFoundException("No se ha encontrado el usuario"));

        if (!client.getEmail().equals(email)) {
            throw new IllegalArgumentException("El usuario no coincide con el email proporcionado");
        }
        Order order = new Order();
        order.setClientId(client);
        order.setRestaurantId(restaurant);
        order.setState(OrderState.pendiente);
        order.setTotal(orderRequestDto.total());
        order.setComments(orderRequestDto.comments());
        order = orderRepository.save(order);

        List<OrderDetails> details = new ArrayList<>();

        for (OrderDetailsRequestDto detailDto : orderRequestDto.details()) {
            Product product = productRepository.findById(detailDto.productId()).orElseThrow(() -> new ProductNotFoundException("Producto no encontrado: " + detailDto.productId()));

            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(detailDto.quantity());
            detail.setSubtotal(detailDto.subtotal());
            details.add(detail);
        }
        orderDetailsRepository.saveAll(details);
        log.info("Pedido creado con éxito");

        List<OrderDetailsResponseDto> detailsResponse = details.stream().map(detail -> new OrderDetailsResponseDto(detail.getOdt_id(), detail.getProduct().getPrd_id(), detail.getQuantity(), detail.getSubtotal())).collect(Collectors.toList());
        return new OrderResponseDto(order.getOrd_id(), order.getClientId().getId(), order.getRestaurantId().getId(), order.getState(), order.getTotal(), order.getComments(), detailsResponse);
    }

    private void validateOrderRequest(OrderRequestDto dto) {
        if (dto.restaurantId() == null || dto.restaurantId() <= 0) {
            throw new IllegalArgumentException("El ID del restaurante no es válido");
        }
        if (dto.clientId() == null || dto.clientId() <= 0) {
            throw new IllegalArgumentException("El ID del cliente no es válido");
        }
        if (dto.details() == null || dto.details().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un detalle de pedido");
        }
        // Validación adicional: verificar que el total coincida con la suma de subtotales
        double calculatedTotal = dto.details().stream().mapToDouble(OrderDetailsRequestDto::subtotal).sum();
        if (!dto.total().equals(calculatedTotal)) {
            throw new IllegalArgumentException("El total del pedido no coincide con la suma de los subtotales");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> findAllOrders(Long restaurantId) {
        log.info("Recuperando todos los pedidos del restaurante atenticado.");
        // Obtener el email del usuario autenticado
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuario autenticado: {}", userEmail);

        // Buscar el restaurante asociado al usuario
        Restaurant restaurant = restaurantRepository.findByIdAndUserEntityEmail(restaurantId, userEmail).orElseThrow(() -> new RestaurantNotFoundException("No se encontró un restaurante asociado al usuario"));
        log.info("Restaurante encontrado con éxito: {}", restaurant);

        // Filtrar pedidos por restaurante
        List<Order> orders = orderRepository.findByRestaurantId(restaurant);

        log.info("Devolviendo {} órdenes para restaurantId: {}", orders.size(), restaurantId);
        return orders.stream().map(order -> new OrderResponseDto(
                order.getOrd_id(),
                order.getClientId().getId(),
                order.getRestaurantId().getId(),
                order.getState(), order.getTotal(),
                order.getComments(),
                order.getDetails().stream().map(
                        detail -> new OrderDetailsResponseDto(
                                detail.getOdt_id(),
                                detail.getProduct().getPrd_id(),
                                detail.getQuantity(),
                                detail.getSubtotal())).collect(Collectors.toList()))).collect(Collectors.toList(

        ));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto findOrderById(Long ord_id) {
        log.info("Buscando el pedido con ID: {}", ord_id);
        if (ord_id == null || ord_id <= 0) {
            log.warn("El ID del pedido proporcionado es invalido: {}", ord_id);
            throw new OrderNotFoundException("El ID del pedido no es válido " + ord_id);
        }

        // Buscar el pedido
        Order order = orderRepository.findById(ord_id).orElseThrow(() -> {
            log.warn("No se encontró el pedido con ID: {}", ord_id);
            return new OrderNotFoundException("Pedido no encontrado con ID: " + ord_id);
        });

        // Obtener el email del usuario autenticado (restaurante)
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuario autenticado: {}", userEmail);

        // Validar que el pedido pertenece al restaurante del usuario autenticado
        if (!order.getRestaurantId().getUserEntity().getEmail().equals(userEmail)) {
            log.warn("Intento de acceso no autorizado al pedido {} por el usuario {}", ord_id, userEmail);
            throw new SecurityException("No tienes permiso para acceder a este pedido");
        }

        log.info("Pedido con ID {} encontrado y autorizado para el restaurante", ord_id);
        return new OrderResponseDto(order.getOrd_id(), order.getClientId().getId(), order.getRestaurantId().getId(), order.getState(), order.getTotal(), order.getComments(), order.getDetails().stream().map(detail -> new OrderDetailsResponseDto(detail.getOdt_id(), detail.getProduct().getPrd_id(), detail.getQuantity(), detail.getSubtotal())).collect(Collectors.toList()));
    }


    @Override
    @Transactional
    //posibles estados de pedido: ('pendiente', 'pagado', 'entregado', 'cancelado')
    public OrderResponseDto updateOrder(Long ord_id, OrderUpdateRequestDto orderUpdateRequestDto) {
        log.info("Intentando actualizar el pedido con ID: {}", ord_id);
        if (ord_id == null || ord_id <= 0) {
            throw new OrderNotFoundException("ID de pedido no válido: " + ord_id);
        }
        Order order = orderRepository.findById(ord_id).orElseThrow(() -> new OrderNotFoundException("Pedido no encontrado con ID: " + ord_id));

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Recuperando el email del usuario de la autenticacion: {}", userEmail);
        log.info("Recuperando el email del usuario dueño del restaurante: {}", order.getRestaurantId().getUserEntity().getEmail());

        if (!order.getRestaurantId().getUserEntity().getEmail().equals(userEmail)) {
            throw new SecurityException("No tienes permiso para editar pedidos de este restaurante");
        }
        log.info("El email del dueño del restuarente es válido: {}", userEmail);


        if (orderUpdateRequestDto.estate() != null) {
            order.setState(orderUpdateRequestDto.estate());
        }
        if (orderUpdateRequestDto.comments() != null) {
            order.setComments(orderUpdateRequestDto.comments());
        }


//        orderDetailsRepository.saveAll(details);
        order = orderRepository.save(order);
        log.info("Pedido actualizado con éxito");


        return new OrderResponseDto(order.getOrd_id(), order.getClientId().getId(), order.getRestaurantId().getId(), order.getState(), order.getTotal(), order.getComments(), order.getDetails().stream().map(detail -> new OrderDetailsResponseDto(detail.getOdt_id(), detail.getProduct().getPrd_id(), detail.getQuantity(), detail.getSubtotal())).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void deleteOrder(Long ord_id) {
        log.info("Intentando eliminar el pedido con ID: {}", ord_id);
        if (ord_id == null || ord_id <= 0) {
            throw new OrderNotFoundException("ID de pedido no válido: " + ord_id);
        }
        // Buscar el pedido
        Order order = orderRepository.findById(ord_id).orElseThrow(() -> {
            log.warn("No se encontró el pedido con ID: {}", ord_id);
            return new OrderNotFoundException("Pedido no encontrado con ID: " + ord_id);
        });

        // Obtener el email del usuario autenticado (restaurante)
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuario autenticado: {}", userEmail);

        // Validar que el pedido pertenece al restaurante del usuario autenticado
        if (!order.getRestaurantId().getUserEntity().getEmail().equals(userEmail)) {
            log.warn("Intento de acceso no autorizado al pedido {} por el usuario {}", ord_id, userEmail);
            throw new SecurityException("No tienes permiso para eliminar este pedido");
        }
        // Validar el estado del pedido
        if (order.getState() != OrderState.pendiente && order.getState() != OrderState.cancelado) {
            log.warn("No se puede eliminar el pedido {} porque está en estado: {}", ord_id, order.getState());
            throw new IllegalStateException("Solo se pueden eliminar pedidos en estado pendiente o cancelado");
        }
        orderRepository.delete(order); // Hibernate eliminará los OrderDetails automáticamente en cascada
        log.info("Pedido con ID {} eliminado con éxito", ord_id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> findByCreatedAtBetween(Long restaurantId, LocalDateTime start, LocalDateTime end) {
        log.info("Recuperando todos los pedidos realizados entre la fecha {} y la fecha {}", start, end);
        // Obtener el email del usuario autenticado (restaurante)
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuario autenticado: {}", userEmail);

        // Buscar el restaurante asociado al usuario
        Restaurant restaurant = restaurantRepository.findByIdAndUserEntityEmail(restaurantId, userEmail).orElseThrow(() -> new RestaurantNotFoundException("No se encontró un restaurante asociado al usuario"));
        log.info("Restaurante encontrado con éxito: {}", restaurant);

        List<Order> orders = orderRepository.findByRestaurantIdAndCreatedAtBetween(restaurant, start, end);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No se encontraron pedidos entre " + start + " y " + end);
        }
        return orders.stream().map(order -> new OrderResponseDto(order.getOrd_id(), order.getClientId().getId(), order.getRestaurantId().getId(), order.getState(), order.getTotal(), order.getComments(), order.getDetails().stream().map(detail -> new OrderDetailsResponseDto(detail.getOdt_id(), detail.getProduct().getPrd_id(), detail.getQuantity(), detail.getSubtotal())).collect(Collectors.toList()))).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> findByClientId(Long cln_id) {
        log.info("Recuperando todos los pedidos del cliente con ID: {}", cln_id);
        if (cln_id == null || cln_id <= 0) {
            throw new OrderNotFoundException("ID de cliente no válido: " + cln_id);
        }
        // Buscar el cliente
        UserEntity client = userRepository.findById(cln_id).orElseThrow(() -> {
            log.warn("No se encontró el cliente con ID: {}", cln_id);
            return new UserNotFoundException("Pedido no encontrado con ID: " + cln_id);
        });
        log.info("Cliente encontrado con éxito: {}", client);
        // Obtener el email del usuario autenticado (restaurante)
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuario autenticado: {}", userEmail);
        log.info("Email del cliente: {}", client.getEmail());

        // Validar que el pedido pertenece al restaurante del usuario autenticado
        if (!client.getEmail().equals(userEmail)) {
            log.warn("Intento de acceso no autorizado al pedido del cliente {} por el usuario {}", cln_id, userEmail);
            throw new SecurityException("No tienes permiso para acceder a los pedidos de este cliente");
        }
        List<Order> orders = orderRepository.findByClientId_Id(cln_id);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No se encontraron pedidos para el cliente con ID: " + cln_id);
        }
        return orders.stream().map(order -> new OrderResponseDto(order.getOrd_id(), order.getClientId().getId(), order.getRestaurantId().getId(), order.getState(), order.getTotal(), order.getComments(), order.getDetails().stream().map(detail -> new OrderDetailsResponseDto(detail.getOdt_id(), detail.getProduct().getPrd_id(), detail.getQuantity(), detail.getSubtotal())).collect(Collectors.toList()))).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> findByClientIdAndCreatedAtBetween(Long cln_id, LocalDateTime start, LocalDateTime end) {
        log.info("Recuperando todos los pedidos del cliente con ID: {} entre las fechas {} y {}", cln_id, start, end);
        if (cln_id == null || cln_id <= 0) {
            throw new UserNotFoundException("ID de cliente no válido: " + cln_id);
        }
        // Obtener el email del usuario autenticado (cliente)
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        String userEmail = "usuario1@example.com";
        log.info("Usuario autenticado: {}", userEmail);

        // Buscar el usuario asociado
        UserEntity client = userRepository.findById(cln_id)
                .orElseThrow(() -> new UserNotFoundException("No se encontró un usuario asociado al usuario"));
        log.info("Cliente encontrado con éxito: {}", client);
        log.info("Email del cliente: {}", client.getEmail());

        // Validar que el pedido pertenece al usuario autenticado
        if (!client.getEmail().equals(userEmail)) {
            log.warn("Intento de acceso no autorizado al pedido del cliente {} por el usuario {}", cln_id, userEmail);
            throw new SecurityException("No tienes permiso para acceder a los pedidos de este cliente");
        }

        List<Order> orders = orderRepository.findByClientId_IdAndCreatedAtBetween(cln_id, start, end);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No se encontraron pedidos entre " + start + " y " + end);
        }
        return orders.stream().map(order ->
                new OrderResponseDto(
                        order.getOrd_id(),
                        order.getClientId().getId(),
                        order.getRestaurantId().getId(),
                        order.getState(),
                        order.getTotal(),
                        order.getComments(),
                        order.getDetails().stream().map(
                                detail -> new OrderDetailsResponseDto(
                                        detail.getOdt_id(),
                                        detail.getProduct().getPrd_id(),
                                        detail.getQuantity(),
                                        detail.getSubtotal())).collect(
                                Collectors.toList()))).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> findByStateAndRestaurantId(OrderState state, Long restaurantId) {
        log.info("Recuperando todos los pedidos en estado: {} del restaurante con ID: {}", state, restaurantId);
        // Obtener el email del usuario autenticado
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        String userEmail = "dueno1@example.com";
        log.info("Usuario autenticado: {}", userEmail);

        // Buscar el restaurante asociado al usuario
        Restaurant restaurant = restaurantRepository.findByIdAndUserEntityEmail(restaurantId, userEmail)
                .orElseThrow(() -> new RestaurantNotFoundException("No se encontró un restaurante asociado al usuario"));
        log.info("Restaurante encontrado con éxito: {}", restaurant);

        List<Order> orders = orderRepository.findByStateAndRestaurantId_Id(state, restaurantId);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No se encontraron pedidos en estado: " + state + " para el restaurante con ID: " + restaurantId);
        }
        return orders.stream()
                .map(order -> new OrderResponseDto(
                        order.getOrd_id(),
                        order.getClientId().getId(),
                        order.getRestaurantId().getId(),
                        order.getState(),
                        order.getTotal(),
                        order.getComments(),
                        order.getDetails().stream()
                                .map(detail -> new OrderDetailsResponseDto(
                                        detail.getOdt_id(),
                                        detail.getProduct().getPrd_id(),
                                        detail.getQuantity(),
                                        detail.getSubtotal()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
