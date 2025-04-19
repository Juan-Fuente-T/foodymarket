package com.c24_39_t_webapp.restaurants.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "productos")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prd_id")
    private Long prd_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prd_restaurante_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prd_categoria_id", nullable = false)
    private Category category;

    @Column(name = "prd_nombre", nullable = false)
    private String name;

    @Column(name = "prd_descripcion")
    private String description;

    @Column(name = "prd_precio", nullable = false)
    private BigDecimal price;

    @Column(name = "prd_imagen")
    private String image;

    @Column(name = "prd_activo", nullable = false)
    private Boolean isActive = true;

    @Column(name = "prd_cantidad", nullable = false)
    private Integer quantity;

    @Column(name = "prd_fecha_alta", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "prd_fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
