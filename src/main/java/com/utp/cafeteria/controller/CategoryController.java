package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.MenuRequest;
import com.utp.cafeteria.dto.MenuResponse;
import com.utp.cafeteria.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuResponse>> obtenerCategorias() {
        return ResponseEntity.ok(menuService.obtenerMenusDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuResponse> obtenerCategoria(@PathVariable UUID id) {
        return ResponseEntity.ok(menuService.obtenerMenu(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponse> crearCategoria(@Valid @RequestBody MenuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.crearMenu(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponse> actualizarCategoria(@PathVariable UUID id, @Valid @RequestBody MenuRequest request) {
        return ResponseEntity.ok(menuService.actualizarMenu(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable UUID id) {
        menuService.eliminarMenu(id);
        return ResponseEntity.noContent().build();
    }
}
