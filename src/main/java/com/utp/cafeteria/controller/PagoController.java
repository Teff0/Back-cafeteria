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

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/procesar")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<PagoResponse> procesarPago(
            @Valid @RequestBody PagoRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        return ResponseEntity.ok(pagoService.procesarPago(request, user.getUsuario().getId()));
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<PagoResponse> obtenerPagoPorPedido(
            @PathVariable java.util.UUID pedidoId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        
        return ResponseEntity.ok(pagoService.obtenerPagoPorPedido(pedidoId, user.getUsuario().getId()));
    }
}