package com.utp.cafeteria.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    private UUID id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;

    /** Categoria a la que pertenece (referencia; puede venir con solo id+nombre). */
    private Categoria categoria;

    /** Subcategoria opcional (referencia; puede venir con solo id+nombre). */
    private Subcategoria subcategoria;

    @Builder.Default
    private Boolean disponible = true;

    private String imagenUrl;

    @Builder.Default
    private Integer stock = 100;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
