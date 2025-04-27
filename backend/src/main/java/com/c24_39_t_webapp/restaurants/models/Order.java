package com.c24_39_t_webapp.restaurants.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pedidos")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pdd_id")
    Long ord_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdd_cliente_id", nullable = false)
    UserEntity clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdd_restaurante_id", nullable = false)
    Restaurant restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pdd_estado", nullable = false)
     OrderStatus status;

    @Column(name = "pdd_total", nullable = false)
     Double total;

    @Column(name = "pdd_comentario")
     String comments;

    @Column(name = "pdd_fecha", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
     LocalDateTime createdAt;

    @Column(name = "pdd_fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp
     LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetails> details;
}

