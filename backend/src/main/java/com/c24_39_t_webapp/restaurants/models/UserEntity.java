package com.c24_39_t_webapp.restaurants.models;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="usuarios")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usr_id")
    private Long id;

    @Column(name = "usr_nombre")
    private String name;

    @Column(name = "usr_email", unique = true)
    private String email;

    @Column(name = "usr_contrasena")
    private String password;

    @Column(name = "usr_tipo")
    private String role;

    @Column(name="usr_telefono")
    private String phone;

    @Column(name="usr_direccion")
    private String address;

    @Column(name="usr_fecha_registro")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
