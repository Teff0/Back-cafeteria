package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.PromoDescuentoRequest;
import com.utp.cafeteria.dto.PromoDescuentoResponse;
import com.utp.cafeteria.service.PromoDescuentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/promos")
@RequiredArgsConstructor
public class PromoDescuentoController {

    private final PromoDescuentoService promoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromoDescuentoResponse>> listar() {
        return ResponseEntity.ok(promoService.obtenerTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PromoDescuentoResponse>> listarActivas() {
        return ResponseEntity.ok(promoService.obtenerActivas());
    }

    @GetMapping("/validar/{codigo}")
    public ResponseEntity<PromoDescuentoResponse> validar(@PathVariable String codigo) {
        return ResponseEntity.ok(promoService.validarCodigo(codigo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoDescuentoResponse> crear(@Valid @RequestBody PromoDescuentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promoService.crear(request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoDescuentoResponse> toggleActivo(@PathVariable UUID id) {
        return ResponseEntity.ok(promoService.toggleActivo(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        promoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
