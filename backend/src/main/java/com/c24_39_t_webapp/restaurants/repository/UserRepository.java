package com.c24_39_t_webapp.restaurants.repository;

import com.c24_39_t_webapp.restaurants.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

//    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM public.restaurant r WHERE r.rst_usuario_id = " +
//            ":restaurantUserId AND EXISTS (SELECT u FROM public.usuarios u WHERE u.usr_id = :userId AND u.usr_id = r.rst_usuario_id)")
//    boolean isTheOwner(@Param("userId") Long userId,
//                                     @Param("restaurantUserId") Long restaurantUserId);
}
