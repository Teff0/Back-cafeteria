package com.utp.cafeteria.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    private UUID id;
    private String nombre;
    private String descripcion;

    @Builder.Default
    private Boolean activo = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
