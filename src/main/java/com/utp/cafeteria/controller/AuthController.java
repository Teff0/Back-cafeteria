package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.exception.UnauthorizedException;
import com.utp.cafeteria.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal com.utp.cafeteria.security.CustomUserDetailsService.CustomUserDetails user) {
        // JWT stateless: el logout real lo hace el cliente descartando el token.
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@AuthenticationPrincipal com.utp.cafeteria.security.CustomUserDetailsService.CustomUserDetails user) {
        if (user == null) {
            throw new UnauthorizedException("Token requerido para refrescar la sesión");
        }
        return ResponseEntity.ok(authService.refreshToken(user.getUsuario()));
    }
}