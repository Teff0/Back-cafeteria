package com.utp.cafeteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponse {
    private String     producto;
    private String     categoria;
    private String     subcategoria;
    private BigDecimal  precio;
    private Integer     stock;
    private Boolean     disponible;
}
