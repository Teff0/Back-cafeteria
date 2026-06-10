package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Notificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {

    private UUID id;
    private Notificacion.TipoNotificacion tipo;
    private String mensaje;
    private Boolean leida;
    private UUID pedidoId;
    private LocalDateTime fecha;

    public static NotificacionResponse from(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .tipo(n.getTipo())
                .mensaje(n.getMensaje())
                .leida(n.getLeida())
                .pedidoId(n.getPedido() != null ? n.getPedido().getId() : null)
                .fecha(n.getFecha())
                .build();
    }
}
