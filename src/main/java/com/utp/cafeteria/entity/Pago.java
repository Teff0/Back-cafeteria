package com.utp.cafeteria.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    private UUID id;
    private Pedido pedido;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estado;
    private String codigoTransaccion;
    private LocalDateTime fechaPago;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum MetodoPago {
        EFECTIVO,
        TARJETA,
        YAPE,
        PLIN
    }

    public enum EstadoPago {
        PENDIENTE,
        COMPLETADO,
        FALLIDO,
        REEMBOLSADO
    }
}
