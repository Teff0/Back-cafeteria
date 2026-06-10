package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.SubcategoriaRequest;
import com.utp.cafeteria.dto.SubcategoriaResponse;
import com.utp.cafeteria.service.SubcategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubcategoriaController {

    private final SubcategoriaService subcategoriaService;

    @GetMapping
    public ResponseEntity<List<SubcategoriaResponse>> listar() {
        return ResponseEntity.ok(subcategoriaService.obtenerTodas());
    }

    @GetMapping("/by-category/{categoriaId}")
    public ResponseEntity<List<SubcategoriaResponse>> listarPorCategoria(@PathVariable UUID categoriaId) {
        return ResponseEntity.ok(subcategoriaService.obtenerPorCategoria(categoriaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubcategoriaResponse> crear(@Valid @RequestBody SubcategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subcategoriaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubcategoriaResponse> actualizar(@PathVariable UUID id,
                                                            @Valid @RequestBody SubcategoriaRequest request) {
        return ResponseEntity.ok(subcategoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        subcategoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
