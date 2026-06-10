package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Subcategoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubcategoriaResponse {

    private UUID id;
    private UUID categoriaId;
    private String categoriaNombre;
    private String nombre;
    private String descripcion;
    private String imagenUrl;

    public static SubcategoriaResponse from(Subcategoria s) {
        return SubcategoriaResponse.builder()
                .id(s.getId())
                .categoriaId(s.getCategoria().getId())
                .categoriaNombre(s.getCategoria().getNombre())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .imagenUrl(s.getImagenUrl())
                .build();
    }
}
