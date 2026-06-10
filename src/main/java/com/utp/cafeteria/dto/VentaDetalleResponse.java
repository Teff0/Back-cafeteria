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
public class VentaDetalleResponse {
    private String     producto;
    private String     categoria;
    private String     subcategoria;
    private Long        cantidadVendida;
    private BigDecimal  costoUnitario;
    private BigDecimal  totalVendido;
}
