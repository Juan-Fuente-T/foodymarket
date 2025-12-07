package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;

//import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory para generar datos de prueba para UserRequestDto y UserResponseDto.
 * Sigue el mismo patrón que ProductFactory y OrderFactory.
 *
 * Cada método retorna NUEVAS instancias para evitar contaminación entre tests.
 */
public final class UserFactory {

    private UserFactory() {
    }

    // ================= REQUEST payload =================

    /**
     * Payload DEFAULT para crear un usuario (usado en POST /api/user).
     * Cada llamada retorna una NUEVA instancia.
     *
     * @return nueva instancia de UserRequestDto con datos por defecto
     */
    public static UserRequestDto defaultRequest() {
        return new UserRequestDto(
                "Juan Pérez",
                "cliente@example.com",
                "CLIENTE",
                "1234567890",
                "Calle de arriba 99",
                "password123"
        );
    }

    /**
     * Payload DEFAULT para crear un usuario con datos personalizados.
     *
     * @param name      Nombre del usuario
     * @param email     Email del usuario
     * @param role      Role del usuario
     * @param phone     Teléfono del usuario
     * @param address   Dirección del usuario
     * @param password  Contraseña del usuario
     * @return nueva instancia de UserRequestDto personalizado
     */
    public static UserRequestDto requestWith(String name, String email, String role, String phone, String address, String password) {
        return new UserRequestDto(
                name,
                email,
                role,
                phone,
                address,
                password
        );
    }
    // ================= RESPONSE payload =================

    /**
     * Payload DEFAULT para la respuesta al crear o actualizar un usuario.
     * Usado en POST /api/user, PUT /api/user y GET /api/user.
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param userId ID del usuario en la respuesta
     * @return nueva instancia de UserResponseDto con datos mapeados
     */
    public static UserResponseDto defaultResponse(Long userId) {
        return new UserResponseDto(
                userId,
                "Juan Pérez",
                "cliente@example.com",
                "CLIENTE",
                "1234567890",
                "Calle de arriba 99"
        );
    }

    /**
     * Genera una Response coherente a partir de un Request.
     *
     * @param req    Datos base de un UserRequestDto
     * @param userId ID del usuario en la respuesta
     * @return nueva instancia de UserResponseDto mapeado desde el request
     */
    public static UserResponseDto responseFromRequest(UserRequestDto req, Long userId) {
//        LocalDateTime now = LocalDateTime.now();
        return new UserResponseDto(
                userId,
                req.name(),
                req.email(),
                "CLIENTE",
                req.phone(),
                req.address()
        );
    }

    /**
     * Genera una lista de responses por defecto para tests que requieren múltiples usuarios.
     *
     * @return List con 3 UserResponseDto por defecto
     */
//    public static List<UserResponseDto> responseListDefault() {
//        List<UserResponseDto> list = new ArrayList<>();
//        list.add(defaultResponse(1L));
//        list.add(new UserResponseDto(2L, "Carlos García","otro@example.com", "CLIENTE", "9876543210", "Calle de abajo 77"));
//        list.add(new UserResponseDto(3L, "Carlos García","otro@example.com", "RESTAURANTE", "24681379", "Calle de del medio 45"));
//        return list;
//    }
}