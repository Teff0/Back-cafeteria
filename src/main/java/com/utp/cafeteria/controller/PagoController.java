package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.security.CustomUserDetailsService;
import com.utp.cafeteria.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<PagoResponse> iniciarPago(
            @Valid @RequestBody PagoRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        return ResponseEntity.ok(pagoService.procesarPago(request, user.getUsuario().getId()));
    }

    @GetMapping("/{orderId}/status")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<PagoResponse> obtenerEstadoPago(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        return ResponseEntity.ok(pagoService.obtenerPagoPorPedido(orderId, user.getUsuario().getId()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhookPago(@RequestBody Object payload) {
        return ResponseEntity.ok().build();
    }
}