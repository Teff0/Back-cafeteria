package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.PromoDescuento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoDescuentoResponse {

    private UUID id;
    private String codigoPromo;
    private String descripcion;
    private PromoDescuento.TipoDescuento tipoDescuento;
    private BigDecimal valor;
    private Integer usoMaximo;
    private Integer usosActuales;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
    private Boolean esValido;

    public static PromoDescuentoResponse from(PromoDescuento p) {
        return PromoDescuentoResponse.builder()
                .id(p.getId())
                .codigoPromo(p.getCodigoPromo())
                .descripcion(p.getDescripcion())
                .tipoDescuento(p.getTipoDescuento())
                .valor(p.getValor())
                .usoMaximo(p.getUsoMaximo())
                .usosActuales(p.getUsosActuales())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .activo(p.getActivo())
                .esValido(p.esValido())
                .build();
    }
}
