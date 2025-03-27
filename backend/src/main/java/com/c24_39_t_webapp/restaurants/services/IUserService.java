package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.models.UserEntity;

public interface IUserService {
    UserResponseDto createUser(UserEntity user);
    UserEntity getUserByEmail(String email);
    UserResponseDto updateUser(UserEntity user);

//    UserResponseDto getUserById(Long id);
    void deleteUser(Long id);

}
