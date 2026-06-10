package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.ResenaRequest;
import com.utp.cafeteria.dto.ResenaResponse;
import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.service.ResenaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaService resenaService;

    @GetMapping("/product/{productoId}")
    public ResponseEntity<List<ResenaResponse>> listarPorProducto(@PathVariable UUID productoId) {
        return ResponseEntity.ok(resenaService.obtenerPorProducto(productoId));
    }

    @GetMapping("/product/{productoId}/rating")
    public ResponseEntity<Map<String, Double>> promedio(@PathVariable UUID productoId) {
        return ResponseEntity.ok(Map.of("promedio", resenaService.promedioCalificacion(productoId)));
    }

    @PostMapping
    public ResponseEntity<ResenaResponse> crear(@Valid @RequestBody ResenaRequest request,
                                                 @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resenaService.crear(request, usuario.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id,
                                          @AuthenticationPrincipal Usuario usuario) {
        resenaService.eliminar(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
