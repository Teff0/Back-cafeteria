package com.utp.cafeteria.controller;

import com.google.common.base.Preconditions;
import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.service.ExcelExportService;
import com.utp.cafeteria.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final int LIMIT_MAXIMO = 100;

    private final ReportService reportService;
    private final ExcelExportService excelExportService;

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> obtenerReporteVentas(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {

        return ResponseEntity.ok(reportService.obtenerReporteVentas(fechaInicio, fechaFin));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<ProductoReporteResponse>> obtenerProductosTop(
            @RequestParam(defaultValue = "10") Integer limit) {

        return ResponseEntity.ok(reportService.obtenerProductosTop(normalizarLimit(limit)));
    }

    @GetMapping("/sales/excel")
    public ResponseEntity<byte[]> exportarVentasExcel(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {

        Map<String, Object> reporte = reportService.obtenerReporteVentas(fechaInicio, fechaFin);
        List<VentaDetalleResponse> detalle = reportService.obtenerDetalleVentas(fechaInicio, fechaFin);
        byte[] excel = excelExportService.exportarReporteVentas(reporte, detalle);
        return excelResponse(excel, "reporte-ventas.xlsx");
    }

    @GetMapping("/inventory/excel")
    public ResponseEntity<byte[]> exportarInventarioExcel() {
        List<InventarioResponse> inventario = reportService.obtenerInventario();
        byte[] excel = excelExportService.exportarInventario(inventario);
        return excelResponse(excel, "reporte-inventario.xlsx");
    }

    @GetMapping("/top-products/excel")
    public ResponseEntity<byte[]> exportarProductosTopExcel(
            @RequestParam(defaultValue = "10") Integer limit) {

        List<ProductoReporteResponse> productos = reportService.obtenerProductosTop(normalizarLimit(limit));
        byte[] excel = excelExportService.exportarProductosTop(productos);
        return excelResponse(excel, "top-productos.xlsx");
    }

    private int normalizarLimit(Integer limit) {
        Preconditions.checkArgument(limit != null && limit > 0, "El parámetro 'limit' debe ser mayor que 0");
        return Math.min(limit, LIMIT_MAXIMO);
    }

    private ResponseEntity<byte[]> excelResponse(byte[] contenido, String nombreArchivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }
}
