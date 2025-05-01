package com.c24_39_t_webapp.restaurants.dtos.response;


public record UserResponseDto (
        Long id,
        String name,
        String email,
        String role,
        String phone,
        String address
) {
}
//@Getter
//@Setter
//@AllArgsConstructor
//public class UserResponseDto {
//    Long id;
//    String name;
//    String email;
//    String role;
//    String phone;
//    String address;
//
//    public UserResponseDto(UserEntity user) {
//        this.id = user.getId();
//        this.name = user.getName();
//        this.email = user.getEmail();
//        this.role = user.getRole();
//        this.phone = user.getPhone();
//        this.address = user.getAddress();
//    }
//
//    // Sobrecarga del contructor para devolver en el login esta clase sin
//    // usar un UserEntity
//    public UserResponseDto(UserDetailsImpl user){
//        this.id = user.getId();
//        this.name = user.getName();
//        this.email = user.getUsername();
//        this.role = user.getRole();
//        this.phone = user.getPhone();
//        this.address = user.getAddress();
//    }

