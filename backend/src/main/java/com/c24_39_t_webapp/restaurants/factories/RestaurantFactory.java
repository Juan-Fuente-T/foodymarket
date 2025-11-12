package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RestaurantFactory {

    private RestaurantFactory() {}

    // Request por defecto (cada llamada -> NUEVA instancia)
    public static RestaurantRequestDto defaultRequest(long id, String email) {
        return new RestaurantRequestDto(
                id,
                "Atlántico",
                "Deliciosas recetas marineras",
                1L,
                "555 666 777",
                email,
                "Calle Arriba 11",
                "10-15 h y 20-24 h",
                "https://example.com/logo.png",
                "https://example.com/cover.jpg"
        );
    }

    // Request para update — solo cambia campos que pases como parámetros (ejemplo simple)
    public static RestaurantRequestDto updatedRequest(long id, String email, String newName) {
        return new RestaurantRequestDto(
                id,
                newName,
                "Nueva descripción",
                1L,
                "555 777 999",
                email,
                "Calle Nueva 33",
                "09-17 h",
                "https://example.com/logo_new.png",
                "https://example.com/cover_new.jpg"
        );
    }

    // Genera una Response coherente a partir de un Request
    public static RestaurantResponseDto responseFromRequest(RestaurantRequestDto req, long responseId) {
        return new RestaurantResponseDto(
                responseId,
                1L,
                req.name(),
                req.description(),
                req.phone(),
                req.email(),
                req.address(),
                req.openingHours(),
                req.logo(),  // puedes mapear o usar req.getLogo()
                req.coverImage(),
                1L,
                "Mediterránea"
        );
    }

    // Crea un request partiendo de otro, aplicando cambios sencillos (si null -> mantiene original)
    public static RestaurantRequestDto updatedRequestFrom(RestaurantRequestDto base, String newName, String newPhone, String newAddress) {
        if (base == null) base = defaultRequest(1L, "test@example.com");
        String name = Objects.requireNonNullElse(newName, base.name());
        String phone = Objects.requireNonNullElse(newPhone, base.phone());
        String address = Objects.requireNonNullElse(newAddress, base.address());
        return new RestaurantRequestDto(
                base.ownerId(),
                name,
                "Descripción actualizada",
                base.ownerId(),
                phone,
                base.email(),
                address,
                base.openingHours(),
                base.logo(),
                base.coverImage()
        );
    }

    /**
     * Crea una lista de RestaurantResponseDto a partir de una lista de RestaurantRequestDto.
     * Cada response se genera con responseFromRequest(request, id).
     * Devuelve UNA LISTA NUEVA (no comparte referencia).
     */
    public static List<RestaurantResponseDto> responseListFromRequests(List<RestaurantRequestDto> requests) {
        List<RestaurantResponseDto> list = new ArrayList<>();
        long id = 1L;
        for (RestaurantRequestDto req : requests) {
            list.add(responseFromRequest(req, id++));
        }
        return list;
    }

    /**
     * Lista por defecto de responses (útil para tests rápidos).
     */
    public static List<RestaurantResponseDto> responseListDefault() {
        List<RestaurantRequestDto> reqs = new ArrayList<>();
        reqs.add(defaultRequest(1L, "atlantic@example.com"));
        // Para el segundo elemento, si se necesita otros datos, crear el request con el nombre cambiado
        // usando updatedRequestFrom (2L, "La Paella", ...)
        RestaurantRequestDto req2 = defaultRequest(2L, "paella@example.com");
        reqs.add(updatedRequestFrom(req2, "La Paella", null, "La calle de enmedio 999"));

        return responseListFromRequests(reqs);
    }
}

//public static List<RestaurantResponseDto> responseListDefault() {
//    List<RestaurantResponseDto> list = new ArrayList<>();
//    list.add(defaultResponse(1L, "test@example.com"));
//    list.add(defaultResponse(2L, "otro@example.com"));
//    return list;
//}

