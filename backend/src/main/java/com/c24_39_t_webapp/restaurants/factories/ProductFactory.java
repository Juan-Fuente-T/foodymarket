package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.GroupedProductsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.request.ProductUpdateDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProductFactory {

    private ProductFactory() {
    }

    // ================= CREATE (REGISTER) payload =================

    /**
     * Payload DEFAULT para crear un producto (usado en POST /api/product).
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param rst_Id ID del restaurante al que pertenece el producto.
     * @return nueva instancia de ProductRequestDto con datos por defecto
     */
    public static ProductRequestDto defaultProductRequest(long rst_Id) {
        return new ProductRequestDto(
                rst_Id,
                1L,
                "Pizza Margherita",
                "Auténtica pizza italiana",
                new BigDecimal("14.99"),
                "https://example.com/pizza-mejorada.jpg",
                true,
                60
        );
    }

    // ================= UPDATE payload =================

    /**
     * Payload DEFAULT para actualizar un producto (usado en PATCH /api/product).
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param rst_Id         Id del restaurante al que pertenece el producto.
     * @param newName        Nuevo nombre del producto.
     * @param newDescription Nueva descripción del producto.
     * @return nueva instancia de ProductUpdateDto con datos por defecto
     */
    public static ProductUpdateDto defaultUpdatedProduct(long rst_Id, String newName, String newDescription) {
        return new ProductUpdateDto(
                rst_Id,
                1L,
                newName,
                newDescription,
                new BigDecimal("14.99"),
                "https://example.com/pizza-mejorada.jpg",
                true,
                60,
                "Pizzas"
        );
    }
    //Similar a defaultUpdatedProduct pero con overrides sencillos

    /**
     * Payload DEFAULT para la respuesta al crear o actualizar un producto a partir de un update request.
     * Usado en POST /api/product y PUT /api/product.
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param base           Datos base de un ProductRequestDto.
     * @param rst_id         Id del restaurante al que pertenece el producto.
     * @param newName        Nuevo nombre del producto (si null, mantiene el original).
     * @param newDescription Nueva descripción del producto (si null, mantiene el original).
     * @param newActive      Nuevo estado activo del producto (si null, mantiene el original).
     * @return nueva instancia de ProductResponseDto con datos mapeados desde el request.
     */
    public static ProductUpdateDto updatedFrom(ProductUpdateDto base,
                                               long rst_id,
                                               String newName,
                                               String newDescription,
                                               Boolean newActive) {
        if (base == null) base = defaultUpdatedProduct(rst_id, newName, newDescription);
        String name = Objects.requireNonNullElse(newName, base.name());
        String description = Objects.requireNonNullElse(newDescription, base.description());
        Boolean isActive = Objects.requireNonNullElse(newActive, base.isActive());
        return new ProductUpdateDto(
                base.restaurantId(),
                base.categoryId(),
                name,
                description,
                base.price(),
                base.image(),
                isActive,
                base.quantity(),
                base.categoryName()
        );
    }

    // ================= RESPONSE payload =================

    /**
     * Payload DEFAULT para la respuesta al crear o actualizar un producto.
     * Usado en POST /api/product y PUT /api/product.
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param base       Datos base de un ProductRequestDto.
     * @param responseId Id de la respuesta del producto creado.
     * @return nueva instancia de ProductResponseDto con datos mapeados desde el request.
     */
    public static ProductResponseDto responseFromRequest(ProductRequestDto base, long responseId) {
        return new ProductResponseDto(
                responseId,
                base.restaurantId(),
                base.categoryId(),
                base.name(),
                base.description(),
                base.price(),
                base.image(),
                base.isActive(),
                base.quantity(),
                "Pizzas",
                "Mediterráneo"
        );
    }

    /**
     * Genera ProductResponseDto a partir de un ProductUpdateDto (uso: PATCH /update).
     * Firma distinta: acepta ProductUpdateDto para evitar errores de tipo.
     */
    public static ProductResponseDto responseFromUpdate(ProductUpdateDto upd, long responseId) {
        return new ProductResponseDto(
                responseId,
                upd.restaurantId(),
                upd.categoryId(),
                upd.name(),
                upd.description(),
                upd.price(),
                upd.image(),
                upd.isActive(),
                upd.quantity(),
                upd.categoryName(),
                "Mediterráneo"
        );
    }

    /**
     * Genera ProductSummaryResponseDto a partir de un ProductUpdateDto (uso: PATCH /update).
     * Cada llamada retorna una nueva instancia.
     *
     * @param responseId Id de la respuesta del producto creado
     * @return nueva instancia de ProductSummaryResponseDto con datos por defecto
     */
    public static ProductSummaryResponseDto responseSummaryDto(long responseId) {
        return new ProductSummaryResponseDto(
                responseId,
                1L,
                1L,
                "Pizza Margherita",
                "Auténtica pizza italiana",
                "https://example.com/pizza.jpg"
        );
    }

    // ================= LIST HELPERS =================

    /**
     * Genera una lista de ProductResponseDto coherente a partir de una lista de ProductRequestDto.
     * Cada response tendrá un id secuencial empezando en startId. Se genera con updatedResponseFromRequest(request, id).
     * Devuelve UNA LISTA NUEVA.
     *
     * @param requests lista de ProductRequestDto (cada uno se convierte en una response coherente)
     * @param startId  id inicial para la primera response (ej. 1L)
     */
    public static List<ProductResponseDto> responseListFromRequests(List<ProductRequestDto> requests, long startId) {
        List<ProductResponseDto> list = new ArrayList<>();
        long id = startId;
        for (ProductRequestDto req : requests) {
            list.add(responseFromRequest(req, id++));
        }
        return list;
    }

    /**
     * Lista por defecto de ProductResponseDto (2 productos).
     * Útil para tests rápidos GET /api/product/all o similares.
     */
    public static List<ProductResponseDto> responseListDefault() {
        ProductRequestDto req1 = defaultProductRequest(1L);
        ProductRequestDto req2 = defaultProductRequest(2L);
        // Modifica el nombre del segundo para que no sea igual al primero
        ProductRequestDto req2Modified = new ProductRequestDto(
                req2.restaurantId(),
                req2.categoryId(),
                "La Paella",
                "Especialidad del restaurante",
                req2.price(),
                req2.image(),
                req2.isActive(),
                req2.quantity()
        );
        return responseListFromRequests(List.of(req1, req2Modified), 1L);
    }

    /**
     * Lista por defecto de ProductSummaryResponseDto(2 productos).
     */
//    public static List<ProductSummaryResponseDto> responseListSummary() {
//        List<ProductSummaryResponseDto> list = new ArrayList<>();
//        long i = 2;
//        for (i = 0; i < 2; i++){
//            list.add(responseSummaryDto(i));
//        };
//        return list;
//    }
    public static List<ProductSummaryResponseDto> responseListSummary() {
        List<ProductSummaryResponseDto> list = new ArrayList<>();

        for (long i = 1; i <= 2; i++) {
            ProductSummaryResponseDto dto = responseSummaryDto(i);
            if (i == 2) {
                dto = new ProductSummaryResponseDto(
                        i,
                        1L,
                        1L,
                        "Pizza Carbonara",
                        "Auténtica carbonara extra",
                        "https://example.com/pizza2.jpg"
                );
            }
            list.add(dto);
        }
        return list;
    }

    /**
     * Lista de ProductResponseDto para "Pastas" (ejemplo).
     * Puedes ajustar ids/valores si quieres otros datos.
     */
    public static List<ProductResponseDto> responseListPastas() {
        return List.of(
                new ProductResponseDto(
                        3L,
                        1L,
                        2L,
                        "Pasta bolognese",
                        "Receta romana",
                        new BigDecimal("13.50"),
                        "https://example.com/pasta.jpg",
                        true,
                        40,
                        "Pastas",
                        "Atlántico"
                )
        );
    }

    /**
     * Devuelve la estructura agrupada por categoría (GroupedProductsResponseDto) por defecto.
     * - Pizzas: usa responseListDefault()
     * - Pastas: usa responseListPastas()
     */
    public static List<GroupedProductsResponseDto> groupedProductsDefault() {
        List<ProductResponseDto> pizzasProducts = responseListDefault();
        List<ProductResponseDto> pastasProducts = responseListPastas();

        GroupedProductsResponseDto pizzasGroup = new GroupedProductsResponseDto(
                "Pizzas",
                1L,
                "Mediterráneo",
                1L,
                pizzasProducts
        );

        GroupedProductsResponseDto pastasGroup = new GroupedProductsResponseDto(
                "Pastas",
                2L,
                "Atlántico",
                2L,
                pastasProducts
        );

        return List.of(pizzasGroup, pastasGroup);
    }
}