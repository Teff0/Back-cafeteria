package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.Pedido;
import com.utp.cafeteria.security.CustomUserDetailsService;
import com.utp.cafeteria.service.CartService;
import com.utp.cafeteria.service.PedidoService;
import com.utp.cafeteria.service.WebSocketNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PedidoService pedidoService;
    private final CartService cartService;
    private final WebSocketNotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> obtenerPedidos(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        String rol = user.getUsuario().getRol().name();
        
        if (rol.equals("ADMIN")) {
            return ResponseEntity.ok(pedidoService.obtenerTodosLosPedidos());
        } else if (rol.equals("CAJA")) {
            return ResponseEntity.ok(pedidoService.obtenerPedidosEnCola());
        } else {
            return ResponseEntity.ok(pedidoService.obtenerMisPedidos(user.getUsuario().getId()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<PedidoResponse> crearPedido(
            @Valid @RequestBody PedidoRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        PedidoResponse response = pedidoService.crearPedido(request, user.getUsuario().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPedido(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        String rol = user.getUsuario().getRol().name();
        
        if (rol.equals("ADMIN") || rol.equals("CAJA")) {
            return ResponseEntity.ok(pedidoService.obtenerPedidoPorId(id));
        }
        
        return ResponseEntity.ok(pedidoService.obtenerPedido(id, user.getUsuario().getId()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJA')")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable UUID id,
            @RequestBody EstadoChangeRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        Pedido.Estado nuevoEstado = Pedido.Estado.valueOf(request.getNuevoEstado());
        PedidoResponse response = pedidoService.cambiarEstado(id, nuevoEstado, user.getUsuario().getId());
        
        notificationService.notificarCambioEstado(id, nuevoEstado.name());
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<PedidoResponse> cancelarPedido(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        return ResponseEntity.ok(pedidoService.cancelarPedido(id, user.getUsuario().getId()));
    }
}