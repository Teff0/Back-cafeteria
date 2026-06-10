package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Categoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponse {

    private UUID id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private Boolean activo;
    private List<SubcategoriaResponse> subcategorias;

    public static CategoriaResponse from(Categoria c) {
        return CategoriaResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .imagenUrl(c.getImagenUrl())
                .activo(c.getActivo())
                .subcategorias(c.getSubcategorias().stream()
                        .map(SubcategoriaResponse::from)
                        .toList())
                .build();
    }
}
