package com.c24_39_t_webapp.restaurants.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


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
    private String category;

    @Column(name = "rst_telefono", nullable = false)
    private String phone;

    @Email(message = "El formato del email no es válido")
    @Column(name="rst_email", unique = true)
    private String email;

    @Column(name = "rst_direccion", nullable = false)
    private String address;

    @Column(name = "rst_horario", nullable = false)
    private String openingHours;

    @Column(name = "rst_logo")
    private String logo;

    @Column(name="rst_foto", length = 1024) // Longitud suficiente para URLs
    private String coverImage;

    @Column(name = "rst_fecha_registro", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "rst_fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "categorias_restaurante", // Nombre de la tabla de unión
            joinColumns = @JoinColumn(
                    name = "restaurante_id", // Nombre de FK en la tabla de unión que apunta a Restaurant
                    referencedColumnName = "rst_id" // REFERENCIA AL PK REAL de Restaurant (`rst_id`)
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "categoria_id",
                    referencedColumnName = "ctg_id"
            )
    )
    private Set<Category> offeredCategories = new HashSet<>();
}
