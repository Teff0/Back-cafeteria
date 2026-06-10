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

    @NotNull(message = "El ID de categoria es requerido")
    private UUID categoriaId;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    private String descripcion;
    private String imagenUrl;
}
