package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.security.CustomUserDetailsService;
import com.utp.cafeteria.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USUARIO')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> obtenerCarrito(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        return ResponseEntity.ok(cartService.obtenerCarrito(user.getUsuario().getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> agregarItem(
            @RequestBody CartItemRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        return ResponseEntity.ok(cartService.agregarItem(request, user.getUsuario().getId()));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartItemResponse> actualizarItem(
            @PathVariable UUID id,
            @RequestBody CartItemRequest request,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        return ResponseEntity.ok(cartService.actualizarItem(id, request, user.getUsuario().getId()));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> eliminarItem(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        cartService.eliminarItem(id, user.getUsuario().getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails user) {
        cartService.vaciarCarrito(user.getUsuario().getId());
        return ResponseEntity.noContent().build();
    }
}