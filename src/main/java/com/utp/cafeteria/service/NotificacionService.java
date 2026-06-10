package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.NotificacionResponse;
import com.utp.cafeteria.entity.Notificacion;
import com.utp.cafeteria.entity.Pedido;
import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.exception.ResourceNotFoundException;
import com.utp.cafeteria.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final WebSocketNotificationService wsNotificationService;

    public List<NotificacionResponse> obtenerMisNotificaciones(UUID usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(NotificacionResponse::from)
                .toList();
    }

    public List<NotificacionResponse> obtenerNoLeidas(UUID usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId).stream()
                .map(NotificacionResponse::from)
                .toList();
    }

    public long contarNoLeidas(UUID usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLeida(UUID id, UUID usuarioId) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", "id", id));
        if (!n.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Notificacion", "id", id);
        }
        n.setLeida(true);
        notificacionRepository.save(n);
    }

    @Transactional
    public void marcarTodasComoLeidas(UUID usuarioId) {
        notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId)
                .forEach(n -> {
                    n.setLeida(true);
                    notificacionRepository.save(n);
                });
    }

    @Transactional
    public void crearNotificacion(Usuario usuario, Pedido pedido,
                                  Notificacion.TipoNotificacion tipo, String mensaje) {
        Notificacion n = Notificacion.builder()
                .usuario(usuario)
                .pedido(pedido)
                .tipo(tipo)
                .mensaje(mensaje)
                .build();
        notificacionRepository.save(n);
        wsNotificationService.notificarUsuario(usuario.getId(), mensaje);
    }
}
