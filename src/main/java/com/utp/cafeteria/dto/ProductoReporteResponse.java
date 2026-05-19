package com.utp.cafeteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoReporteResponse {
    private UUID productoId;
    private String productoNombre;
    private Long cantidadVendida;
    private Double revenueTotal;
}