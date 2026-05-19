package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.MenuResponse;
import com.utp.cafeteria.entity.Menu;
import com.utp.cafeteria.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<MenuResponse> crearCategoria(@RequestBody Menu menu) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.crearMenu(menu));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponse> actualizarCategoria(@PathVariable UUID id, @RequestBody Menu menu) {
        return ResponseEntity.ok(menuService.actualizarMenu(id, menu));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable UUID id) {
        menuService.eliminarMenu(id);
        return ResponseEntity.noContent().build();
    }
}