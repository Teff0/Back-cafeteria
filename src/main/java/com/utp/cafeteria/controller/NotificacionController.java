package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.NotificacionResponse;
import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(notificacionService.obtenerMisNotificaciones(usuario.getId()));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificacionResponse>> noLeidas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(notificacionService.obtenerNoLeidas(usuario.getId()));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(Map.of("count", notificacionService.contarNoLeidas(usuario.getId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> marcarLeida(@PathVariable UUID id,
                                             @AuthenticationPrincipal Usuario usuario) {
        notificacionService.marcarComoLeida(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> marcarTodasLeidas(@AuthenticationPrincipal Usuario usuario) {
        notificacionService.marcarTodasComoLeidas(usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
