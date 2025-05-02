package com.c24_39_t_webapp.restaurants.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@Table(name = "detalles_pedido")
public class OrderDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dtp_id")
    private Long odt_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dtp_pedido_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dtp_producto_id", nullable = false)
    private Product product;

    @Column(name = "dtp_cantidad", nullable = false)
    private Integer quantity;

    @Column(name = "dtp_subtotal", nullable = false)
    private BigDecimal subtotal;
}