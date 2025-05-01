package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;

public interface IUserService {
    UserResponseDto createUser(UserRequestDto registerDto);
    UserResponseDto getUserProfile(String userEmail);
    UserResponseDto updateUser(Long userIdToUpdate, UserRequestDto userRequestDto);

//    UserResponseDto getUserById(Long id);
    void deleteUser(Long userId);

}
