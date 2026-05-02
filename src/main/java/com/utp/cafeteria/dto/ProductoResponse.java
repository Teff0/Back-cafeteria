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
    private Producto.Categoria categoria;
    private Boolean disponible;
    private String imagenUrl;
    private Integer stock;

    public static ProductoResponse from(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .categoria(producto.getCategoria())
                .disponible(producto.getDisponible())
                .imagenUrl(producto.getImagenUrl())
                .stock(producto.getStock())
                .build();
    }
}