package com.utp.cafeteria.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public void notificarCambioEstado(UUID pedidoId, String nuevoEstado) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/pedidos",
                    new NotificacionMensaje("ESTADO_CAMBIADO", pedidoId.toString(), nuevoEstado)
            );
        } catch (Exception e) {
            logger.error("Error al notificar cambio de estado del pedido {}: {}", pedidoId, e.getMessage());
        }
    }

    public void notificarPedidoCreado(UUID pedidoId, String usuarioEmail) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/admin/pedidos",
                    new NotificacionMensaje("PEDIDO_CREADO", pedidoId.toString(), usuarioEmail)
            );
        } catch (Exception e) {
            logger.error("Error al notificar pedido creado {}: {}", pedidoId, e.getMessage());
        }
    }

    public void notificarPedidoListo(UUID pedidoId) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/pedidos/" + pedidoId,
                    new NotificacionMensaje("PEDIDO_LISTO", pedidoId.toString(), "Su pedido está listo para recoger")
            );
        } catch (Exception e) {
            logger.error("Error al notificar pedido listo {}: {}", pedidoId, e.getMessage());
        }
    }

    public void notificarUsuario(UUID usuarioId, String mensaje) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/usuario/" + usuarioId,
                    new NotificacionMensaje("NOTIFICACION", usuarioId.toString(), mensaje)
            );
        } catch (Exception e) {
            logger.error("Error al notificar usuario {}: {}", usuarioId, e.getMessage());
        }
    }

    public record NotificacionMensaje(
            String tipo,
            String referencia,
            String mensaje
    ) {}
}