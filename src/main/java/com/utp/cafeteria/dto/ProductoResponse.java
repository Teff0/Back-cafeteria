package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {

    private UUID id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private UUID categoriaId;
    private String categoriaNombre;
    private UUID subcategoriaId;
    private String subcategoriaNombre;
    private Boolean disponible;
    private String imagenUrl;
    private Integer stock;
    private Integer tiempoPreparacion;
    private String horarioDisponible;

    public static ProductoResponse from(Producto p) {
        return ProductoResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .categoriaId(p.getCategoria().getId())
                .categoriaNombre(p.getCategoria().getNombre())
                .subcategoriaId(p.getSubcategoria() != null ? p.getSubcategoria().getId() : null)
                .subcategoriaNombre(p.getSubcategoria() != null ? p.getSubcategoria().getNombre() : null)
                .disponible(p.getDisponible())
                .imagenUrl(p.getImagenUrl())
                .stock(p.getStock())
                .tiempoPreparacion(p.getTiempoPreparacion())
                .horarioDisponible(p.getHorarioDisponible())
                .build();
    }
}
