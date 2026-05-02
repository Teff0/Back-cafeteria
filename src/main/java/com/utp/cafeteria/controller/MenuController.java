package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/hoy")
    public ResponseEntity<MenuResponse> obtenerMenuDelDia() {
        return ResponseEntity.ok(menuService.obtenerMenuDelDia());
    }

    @GetMapping
    public ResponseEntity<List<MenuResponse>> obtenerMenusDisponibles() {
        return ResponseEntity.ok(menuService.obtenerMenusDisponibles());
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<MenuResponse>> obtenerMenusPorFecha(@PathVariable LocalDate fecha) {
        return ResponseEntity.ok(menuService.obtenerMenusPorFecha(fecha));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuResponse> obtenerMenu(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(menuService.obtenerMenu(id));
    }
}