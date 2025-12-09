package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.response.CuisineResponseDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory para generar datos de prueba para CuisineResponseDto.
 * Sigue el mismo patrón que ProductFactory, OrderFactory y UserFactory.
 *
 * Cada método retorna NUEVAS instancias para evitar contaminación entre tests.
 */
public final class RestaurantCuisineFactory {

    private RestaurantCuisineFactory() {
    }

    // ================= RESPONSE payload =================

    /**
     * Genera un CuisineResponseDto por defecto.
     *
     * @param cuisineId ID de la cocina
     * @param name      Nombre de la cocina
     * @return nueva instancia de CuisineResponseDto
     */
    public static CuisineResponseDto defaultResponse(Long cuisineId, String name) {
        return new CuisineResponseDto(
                cuisineId,
                name
        );
    }

    /**
     * Payload DEFAULT para la respuesta al obtener todas las cuisines.
     * Usado en GET /api/cuisines.
     * Cada llamada retorna una NUEVA instancia.
     *
     * @return nueva instancia de CuisineResponseDto con datos por defecto
     */
//    public static CuisineResponseDto defaultCuisineResponse() {
//        return new CuisineResponseDto(
//                1L,
//                "Mediterránea"
//        );
//    }

    /**
     * Genera una lista de responses por defecto para tests que requieren múltiples cuisines.
     *
     * @return List con 5 CuisineResponseDto por defecto (tipos de cocina típicos)
     */
    public static List<CuisineResponseDto> responseListDefault() {
        List<CuisineResponseDto> list = new ArrayList<>();
        list.add(new CuisineResponseDto(1L, "Mediterránea"));
        list.add(new CuisineResponseDto(2L, "Italiana"));
        list.add(new CuisineResponseDto(3L, "Asiática"));
        list.add(new CuisineResponseDto(4L, "Americana"));
        list.add(new CuisineResponseDto(5L, "Española"));
        return list;
    }
}