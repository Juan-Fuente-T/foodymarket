package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.request.OrderDetailsRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderUpdateRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderDetailsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.models.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory para generar datos de prueba para OrderRequestDto, OrderUpdateRequestDto y OrderResponseDto.
 * Sigue el mismo patrón que RestaurantFactory, ProductFactory y CategoryFactory.
 *
 * Cada método retorna NUEVAS instancias para evitar contaminación entre tests.
 */
public final class OrderFactory {

    private OrderFactory() {
    }

    // ================= REQUEST payload =================

    /**
     * Genera un OrderDetailsRequestDto por defecto para incluir en una orden.
     *
     * @param productId ID del producto
     * @param quantity  Cantidad del producto
     * @param subtotal  Subtotal para este detalle
     * @return nueva instancia de OrderDetailsRequestDto
     */
    public static OrderDetailsRequestDto defaultOrderDetail(Long productId, Integer quantity, BigDecimal subtotal) {
        return new OrderDetailsRequestDto(
                productId,
                quantity,
                subtotal
        );
    }

    /**
     * Payload DEFAULT para crear una orden (usado en POST /api/order).
     * Incluye un detalle por defecto.
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param restaurantId ID del restaurante
     * @param clientId     ID del cliente
     * @return nueva instancia de OrderRequestDto con datos por defecto
     */
    public static OrderRequestDto defaultRequest(Long restaurantId, Long clientId) {
        List<OrderDetailsRequestDto> details = new ArrayList<>();
        details.add(defaultOrderDetail(1L, 2, new BigDecimal("29.98")));

        return new OrderRequestDto(
                1L,  // orderDetailsId
                clientId,
                restaurantId,
                OrderStatus.pendiente,
                new BigDecimal("29.98"),  // total (debe coincidir con suma de subtotales)
                "Sin instrucciones especiales",
                details
        );
    }

    /**
     * Payload DEFAULT para crear una orden con múltiples detalles.
     *
     * @param restaurantId ID del restaurante
     * @param clientId     ID del cliente
     * @param details      Lista de OrderDetailsRequestDto
     * @return nueva instancia de OrderRequestDto con detalles personalizados
     */
    public static OrderRequestDto requestWithDetails(Long restaurantId, Long clientId, List<OrderDetailsRequestDto> details) {
        BigDecimal total = details.stream()
                .map(OrderDetailsRequestDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderRequestDto(
                1L,
                clientId,
                restaurantId,
                OrderStatus.pendiente,
                total,
                "Pedido personalizado",
                details
        );
    }

    // ================= UPDATE REQUEST payload =================

    /**
     * Payload DEFAULT para actualizar una orden (usado en PATCH /api/order/{id}).
     * Cada llamada retorna una NUEVA instancia.
     *
     * @return nueva instancia de OrderUpdateRequestDto con estado CONFIRMADO
     */
    public static OrderUpdateRequestDto defaultUpdateRequest() {
        return new OrderUpdateRequestDto(
                OrderStatus.pagado,
                "Pedido pagado"
        );
    }

    /**
     * Payload para actualizar una orden con estado específico.
     *
     * @param status   Nuevo estado del pedido
     * @param comments Nuevos comentarios
     * @return nueva instancia de OrderUpdateRequestDto personalizado
     */
    public static OrderUpdateRequestDto updateRequestWith(OrderStatus status, String comments) {
        return new OrderUpdateRequestDto(
                status,
                comments
        );
    }

    // ================= RESPONSE payload =================

    /**
     * Genera un OrderDetailsResponseDto por defecto.
     *
     * @param detailId  ID del detalle
     * @param productId ID del producto
     * @param quantity  Cantidad
     * @param subtotal  Subtotal
     * @return nueva instancia de OrderDetailsResponseDto
     */
    public static OrderDetailsResponseDto defaultOrderDetailResponse(Long detailId, Long productId, Integer quantity, BigDecimal subtotal) {
        return new OrderDetailsResponseDto(
                detailId,
                productId,
                "Pizza Margherita",
                quantity,
                new BigDecimal("14.99"),
                subtotal
        );
    }

    /**
     * Payload DEFAULT para la respuesta al crear o actualizar una orden.
     * Usado en POST /api/order y PATCH /api/order/{id}.
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param orderId      ID de la orden en la respuesta
     * @param clientId     ID del cliente
     * @param restaurantId ID del restaurante
     * @return nueva instancia de OrderResponseDto con datos mapeados
     */
    public static OrderResponseDto defaultResponse(Long orderId, Long clientId, Long restaurantId) {
        List<OrderDetailsResponseDto> details = new ArrayList<>();
        details.add(defaultOrderDetailResponse(1L, 1L, 2, new BigDecimal("29.98")));

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ISO_DATE_TIME);

        return new OrderResponseDto(
                orderId,
                clientId,
                restaurantId,
                "Atlántico",  // restaurantName
                OrderStatus.pendiente,
                new BigDecimal("29.98"),
                "Sin instrucciones especiales",
                details,
                timestamp,
                timestamp
        );
    }

    /**
     * Genera una Response coherente a partir de un Request.
     *
     * @param req       Datos base de un OrderRequestDto
     * @param responseId ID de la orden en la respuesta
     * @return nueva instancia de OrderResponseDto mapeado desde el request
     */
    public static OrderResponseDto responseFromRequest(OrderRequestDto req, Long responseId) {
        List<OrderDetailsResponseDto> detailsResponse = new ArrayList<>();

        // Mapea los detalles del request a response
        for (int i = 0; i < req.details().size(); i++) {
            OrderDetailsRequestDto detailReq = req.details().get(i);
            detailsResponse.add(new OrderDetailsResponseDto(
                    (long) i + 1,
                    detailReq.productId(),
                    "Pizza Margherita",
                    detailReq.quantity(),
                    new BigDecimal("14.99"),
                    detailReq.subtotal()
            ));
        }

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ISO_DATE_TIME);

        return new OrderResponseDto(
                responseId,
                req.clientId(),
                req.restaurantId(),
                "Atlántico",
                req.status(),
                req.total(),
                req.comments(),
                detailsResponse,
                timestamp,
                timestamp
        );
    }

    /**
     * Genera una lista de responses por defecto para tests que requieren múltiples órdenes.
     *
     * @return List con 3 OrderResponseDto por defecto
     */
    public static List<OrderResponseDto> responseListDefault() {
        List<OrderResponseDto> list = new ArrayList<>();
        list.add(defaultResponse(1L, 10L, 1L));
        list.add(defaultResponse(2L, 11L, 1L));
        list.add(defaultResponse(3L, 12L, 1L));
        return list;
    }
}