package com.utp.cafeteria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubcategoriaRequest {

    @NotNull(message = "La categoria es requerida")
    private UUID categoriaId;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    private String descripcion;
}
