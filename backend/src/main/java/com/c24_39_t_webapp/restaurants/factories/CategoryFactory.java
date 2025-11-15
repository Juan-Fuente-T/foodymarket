package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Factory para generar datos de prueba para CategoryRequestDto y CategoryResponseDto.
 * Sigue el mismo patrón que RestaurantFactory y ProductFactory.
 *
 * Cada método retorna NUEVAS instancias para evitar contaminación entre tests.
 */
public final class CategoryFactory {

    private CategoryFactory() {
    }

    // ================= REQUEST payload =================

    /**
     * Payload DEFAULT para crear una categoría (usado en POST /api/restaurant/{id}/categories).
     * Cada llamada retorna una NUEVA instancia.
     *
     * @return nueva instancia de CategoryRequestDto con datos por defecto
     */
    public static CategoryRequestDto defaultRequest() {
        return new CategoryRequestDto(
                "Pizzas",
                "Deliciosas pizzas artesanales"
        );
    }

    /**
     * Payload DEFAULT para crear una categoría con parámetros personalizables.
     * Permite personalizar nombre y descripción.
     *
     * @param name        Nombre de la categoría
     * @param description Descripción de la categoría
     * @return nueva instancia de CategoryRequestDto con datos personalizados
     */
//    public static CategoryRequestDto requestWith(String name, String description) {
//        return new CategoryRequestDto(
//                name,
//                description
//        );
//    }

    // ================= RESPONSE payload =================

    /**
     * Payload DEFAULT para la respuesta al crear o asociar una categoría.
     * Usado en POST /api/restaurant/{id}/categories.
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param categoryId ID de la categoría en la respuesta
     * @return nueva instancia de CategoryResponseDto con datos mapeados
     */
    public static CategoryResponseDto defaultResponse(long categoryId) {
        return new CategoryResponseDto(
                categoryId,
                "Pizzas",
                "Deliciosas pizzas artesanales"
        );
    }

    /**
     * Genera una Response coherente a partir de un Request.
     * Mapea todos los campos del request al response y asigna el ID de respuesta.
     *
     * @param req        Datos base de un CategoryRequestDto
     * @param responseId ID de la respuesta de la categoría
     * @return nueva instancia de CategoryResponseDto mapeada desde el request
     */
    public static CategoryResponseDto responseFromRequest(CategoryRequestDto req, long responseId) {
        return new CategoryResponseDto(
                responseId,
                req.name(),
                req.description()
        );
    }

    /**
     * Payload DEFAULT para la respuesta con parámetros personalizables.
     * Permite personalizar ID, nombre y descripción.
     *
     * @param categoryId  ID de la categoría en la respuesta
     * @param name        Nombre de la categoría
     * @param description Descripción de la categoría
     * @return nueva instancia de CategoryResponseDto con datos personalizados
     */
    public static CategoryResponseDto responseWith(long categoryId, String name, String description) {
        return new CategoryResponseDto(
                categoryId,
                name,
                description
        );
    }

    /**
     * Genera una lista de responses por defecto.
     * Útil para tests que requieren múltiples categorías (por ejemplo, getAllCategories).
     *
     * @return List con 3 CategoryResponseDto por defecto
     */
    public static List<CategoryResponseDto> responseListDefault() {
        List<CategoryResponseDto> list = new ArrayList<>();
        list.add(responseWith(1L, "Pizzas", "Deliciosas pizzas artesanales"));
        list.add(responseWith(2L, "Pastas", "Variedad de pastas italianas"));
        list.add(responseWith(3L, "Postres", "Deliciosos postres caseros"));
        return list;
    }

    /**
     * Genera un Set de responses por defecto.
     * Útil para tests que requieren un Set de categorías (como en getOfferedCategories).
     * Retorna un Set porque getOfferedCategories retorna Set<CategoryResponseDto>.
     *
     * @return Set con 2 CategoryResponseDto por defecto
     */
    public static Set<CategoryResponseDto> responseSetDefault() {
        Set<CategoryResponseDto> set = new HashSet<>();
        set.add(responseWith(1L, "Pizzas", "Deliciosas pizzas artesanales"));
        set.add(responseWith(2L, "Pastas", "Variedad de pastas italianas"));
        return set;
    }
}