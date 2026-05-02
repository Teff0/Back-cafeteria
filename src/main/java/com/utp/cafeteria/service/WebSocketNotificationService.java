package com.utp.cafeteria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notificarCambioEstado(UUID pedidoId, String nuevoEstado) {
        messagingTemplate.convertAndSend(
                "/topic/pedidos",
                new NotificacionMensaje("ESTADO_CAMBIADO", pedidoId.toString(), nuevoEstado)
        );
    }

    public void notificarPedidoCreado(UUID pedidoId, String usuarioEmail) {
        messagingTemplate.convertAndSend(
                "/topic/admin/pedidos",
                new NotificacionMensaje("PEDIDO_CREADO", pedidoId.toString(), usuarioEmail)
        );
    }

    public void notificarPedidoListo(UUID pedidoId) {
        messagingTemplate.convertAndSend(
                "/topic/pedidos/" + pedidoId.toString(),
                new NotificacionMensaje("PEDIDO_LISTO", pedidoId.toString(), "Su pedido está listo para recoger")
        );
    }

    public void notificarUsuario(UUID usuarioId, String mensaje) {
        messagingTemplate.convertAndSend(
                "/topic/usuario/" + usuarioId.toString(),
                new NotificacionMensaje("NOTIFICACION", usuarioId.toString(), mensaje)
        );
    }

    public record NotificacionMensaje(
            String tipo,
            String referencia,
            String mensaje
    ) {}
}