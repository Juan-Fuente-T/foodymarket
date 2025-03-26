package com.c24_39_t_webapp.restaurants.models;

import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@Table(name = "restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rst_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rst_usuario_id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "rst_nombre", nullable = false)
    private String name;

    @Column(name = "rst_descripcion", nullable = false)
    private String description;

    @Column(name = "rst_categoria", nullable = false)
    private String categoria;

    @Column(name = "rst_telefono", nullable = false)
    private String phone;

    @Column(name = "rst_direccion", nullable = false)
    private String address;

    @Column(name = "rst_logo")
    private String logo;

    @Column(name = "rst_fecha_registro", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "rst_fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Restaurant(RestaurantRequestDto restaurantRequestDto) {
        this.name = restaurantRequestDto.name();
        this.description = restaurantRequestDto.description();
        this.phone = restaurantRequestDto.phone();
        this.address = restaurantRequestDto.address();
        this.logo = restaurantRequestDto.logo();
    }
}
