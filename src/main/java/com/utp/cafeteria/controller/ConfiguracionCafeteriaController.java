package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.ConfiguracionCafeteriaRequest;
import com.utp.cafeteria.dto.ConfiguracionCafeteriaResponse;
import com.utp.cafeteria.service.ConfiguracionCafeteriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfiguracionCafeteriaController {

    private final ConfiguracionCafeteriaService configService;

    @GetMapping
    public ResponseEntity<ConfiguracionCafeteriaResponse> obtener() {
        return ResponseEntity.ok(configService.obtener());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionCafeteriaResponse> guardar(
            @Valid @RequestBody ConfiguracionCafeteriaRequest request) {
        return ResponseEntity.ok(configService.guardar(request));
    }

    @PatchMapping("/toggle-pedidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionCafeteriaResponse> toggleAceptaPedidos() {
        return ResponseEntity.ok(configService.toggleAceptaPedidos());
    }
}
