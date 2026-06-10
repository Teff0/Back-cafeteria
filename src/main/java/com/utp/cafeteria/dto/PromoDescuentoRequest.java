package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.PromoDescuento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoDescuentoRequest {

    @NotBlank(message = "El código de promo es requerido")
    private String codigoPromo;

    private String descripcion;

    @NotNull(message = "El tipo de descuento es requerido")
    private PromoDescuento.TipoDescuento tipoDescuento;

    @NotNull(message = "El valor es requerido")
    @Positive(message = "El valor debe ser mayor a 0")
    private BigDecimal valor;

    private Integer usoMaximo;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate fechaFin;
}
