package com.utp.cafeteria.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subcategoria {

    private UUID id;
    private UUID categoriaId;
    private String nombre;
    private String descripcion;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
