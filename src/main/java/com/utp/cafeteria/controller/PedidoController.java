package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.Pedido;
import com.utp.cafeteria.security.CustomUserDetailsService;
import com.utp.cafeteria.service.PedidoService;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<PedidoResponse> crearPedido(
            @Valid @RequestBody PedidoRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        PedidoResponse response = pedidoService.crearPedido(request, user.getUsuario().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<List<PedidoResponse>> obtenerMisPedidos(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        return ResponseEntity.ok(pedidoService.obtenerMisPedidos(user.getUsuario().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPedido(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        return ResponseEntity.ok(pedidoService.obtenerPedido(id, user.getUsuario().getId()));
    }
}