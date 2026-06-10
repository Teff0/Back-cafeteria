package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Pago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {

    private UUID id;
    private UUID pedidoId;
    private UUID usuarioId;
    private BigDecimal monto;
    private Pago.MetodoPago metodoPago;
    private Pago.EstadoPago estado;
    private String codigoTransaccion;
    private String comprobanteUrl;
    private LocalDateTime fechaPago;
    private LocalDateTime createdAt;
}
