package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.IUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public UserResponseDto createUser(UserEntity user) {
        log.info("Creando un nuevo usuario con email: {}", user.getEmail());
        // Codifica la contraseña antes de guardarla
        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setCreatedAt();
        UserEntity savedUser = userRepository.save(user);
        log.info("Usuario creado exitosamente con ID: {}", savedUser.getId());
        return new UserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(UserEntity user) {
        log.info("Actualizando el usuario con email: {}", user.getEmail());

        // Obtener el usuario autenticado (el que está haciendo la petición)
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Verificar si el usuario autenticado es el mismo que el usuario que se está actualizando
        if (!authenticatedUsername.equals(user.getEmail())) {
            log.warn("Intento de actualizar el perfil de otro usuario. Usuario autenticado: {}, Usuario a actualizar: {}", authenticatedUsername, user.getEmail());
            throw new AccessDeniedException("No tienes permiso para actualizar el perfil de otro usuario.");
        }
        // Codificar la contraseña si se proporciona una nueva
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        UserEntity updatedUser = userRepository.save(user);

        log.info("Usuario actualizado exitosamente");
        return new UserResponseDto(updatedUser);
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        log.info("Obteniendo usuario por email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("El Usuario no Existe!"));
    }

//    public List<UserResponseDto> getAllUsers() {
//        log.info("Obteniendo todos los usuarios");
//        List<UserEntity> users = userRepository.findAll();
//        return users.stream()
//                .map(UserResponseDto::new)
//                .collect(Collectors.toList());
//    }
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Eliminando usuario con ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("No se encontró el usuario con ID: {}", id);
            throw new UserNotFoundException("No se encontró el usuario con ID: " + id);
        }
        userRepository.deleteById(id);
        log.info("Usuario eliminado exitosamente con ID: {}", id);
    }
}
