package com.c24_39_t_webapp.restaurants.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
@NotBlank(message = "El nombre es obligatorio")
String name,
@NotBlank(message = "El email es obligatorio")
@Email(message = "El email debe ser válido")
String email,
@NotBlank()
String role,
String phone,
@NotBlank(message = "La dirección es obligatoria")
String address,

@NotBlank(message = "La contraseña es obligatoria")
@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
String password
) {
}
