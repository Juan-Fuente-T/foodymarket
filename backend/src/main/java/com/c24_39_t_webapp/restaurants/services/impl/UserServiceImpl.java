package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.IUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto registerDto) {
        log.info("Creando un nuevo usuario con email: {}", registerDto.email());

        if(userRepository.existsByEmail(registerDto.email())){
            log.warn("Intento de registro con email duplicado: {}", registerDto.email());
            try {
                throw new IllegalAccessException("El email ya está registrado.");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        UserEntity newUser = new UserEntity();
        newUser.setName(registerDto.name());
        newUser.setEmail(registerDto.email());
        newUser.setPhone(registerDto.phone());
        newUser.setAddress(registerDto.address());
        // Codificar la contraseña SIEMPRE al crear
        newUser.setPassword(passwordEncoder.encode(registerDto.password()));

        String requestedRole = registerDto.role() != null ? registerDto.role().toLowerCase() : "cliente"; // Default a cliente si no viene
        if (!requestedRole.equals("cliente") && !requestedRole.equals("restaurante")) {
            log.warn("Intento de registro con rol inválido: {}", registerDto.role());
            throw new IllegalArgumentException("Rol de usuario inválido.");
        }
        newUser.setRole(requestedRole);

        // Guardar la NUEVA entidad
        UserEntity savedUser = userRepository.save(newUser);
        log.info("Usuario creado exitosamente con ID: {}", savedUser.getId());

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getPhone(),
                savedUser.getAddress()
        );
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long userIdToUpdate, UserRequestDto updateDto) {
        log.info("Actualizando el usuario con email: {}", updateDto.name());

        UserEntity existingUser = userRepository.findById(userIdToUpdate)
                .orElseThrow(() -> new UserNotFoundException("El Usuario no Existe!")
        );
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUsername = authentication.getName();
        // Verificar si el usuario autenticado es el mismo que el usuario que se está actualizando
        if (!existingUser.getEmail().equals(authenticatedUsername)) {
            log.warn("Usuario {} intentando actualizar usuario {} sin permiso.", authenticatedUsername, userIdToUpdate);
            throw new AccessDeniedException("No tienes permiso para actualizar este perfil.");
        }
        if (updateDto.name() != null) { // Asume que UserUpdateDto tiene estos getters
            existingUser.setName(updateDto.name());
        }
        if (updateDto.phone() != null) {
            existingUser.setPhone(updateDto.phone());
        }
        if (updateDto.address() != null) {
            existingUser.setAddress(updateDto.address());
        }

        // Actualizar contraseña SOLO si se proporciona una nueva en el DTO
        if (updateDto.password() != null && !updateDto.password().isEmpty()) {
            log.info("Actualizando contraseña para usuario ID: {}", userIdToUpdate);
            existingUser.setPassword(passwordEncoder.encode(updateDto.password()));
        }

        UserEntity savedUser = userRepository.save(existingUser);
        log.info("Usuario ID {} actualizado exitosamente", savedUser.getId());

        return new UserResponseDto(
                existingUser.getId(),
                existingUser.getName(),
                existingUser.getEmail(),
                existingUser.getRole(),
                existingUser.getPhone(),
                existingUser.getAddress()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserProfile(String userEmail) {
        log.info("Obteniendo usuario por email: {}", userEmail);
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("El Usuario no Existe!"));
        log.info("Usuario encontrado: {}", user.getEmail());

        UserResponseDto dto = new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAddress()
        );
        log.info("DTO creado en findById ANTES de retornar: {}", dto);
        return dto;
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
        //  REVISAR CON CUIDADO
//        log.info("Eliminando usuario con ID: {}", id);
//        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
//        UserEntity user = userRepository.findById(id)
//                .orElseThrow(() -> new UserNotFoundException("El usuario con el siguiente ID no ha sido encontrado: " + id));
//        if (!user.getEmail().equals(authenticatedUsername /* && !SecurityUtils.isAdmin() */ )) { // Añadir lógica si admin puede ver
//            log.warn("Usuario {} no tiene permiso para borrar al usuario {}", authenticatedUsername, id);
//            throw new SecurityException("No tienes permiso para borrar a este usuario.");
//        }
//        userRepository.deleteById(id);
//        log.info("Usuario eliminado exitosamente con ID: {}", id);
    }
}
