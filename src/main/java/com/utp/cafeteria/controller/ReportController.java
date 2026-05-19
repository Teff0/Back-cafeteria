package com.utp.cafeteria.controller;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> obtenerReporteVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {
        
        return ResponseEntity.ok(reportService.obtenerReporteVentas(fechaInicio, fechaFin));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<ProductoReporteResponse>> obtenerProductosTop(
            @RequestParam(defaultValue = "10") Integer limit) {
        
        return ResponseEntity.ok(reportService.obtenerProductosTop(limit));
    }
}