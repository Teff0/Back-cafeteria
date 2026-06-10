package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.ProductoReporteResponse;
import com.utp.cafeteria.repository.*;
import com.utp.cafeteria.util.XlsxWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProductoRepository productoRepository;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Map<String, Object> obtenerReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null) fechaInicio = LocalDate.now().minusDays(30);
        if (fechaFin == null) fechaFin = LocalDate.now();

        var pedidos = pedidoRepository.findByFechaRangoYEstado(
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(LocalTime.MAX)
        );

        double totalVentas = pedidos.stream()
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal().doubleValue() : 0.0)
                .sum();

        long totalPedidos = pedidos.size();

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("fechaInicio", fechaInicio.toString());
        reporte.put("fechaFin", fechaFin.toString());
        reporte.put("totalVentas", totalVentas);
        reporte.put("totalPedidos", totalPedidos);
        reporte.put("promedioPorPedido", totalPedidos > 0 ? totalVentas / totalPedidos : 0);

        return reporte;
    }

    public List<ProductoReporteResponse> obtenerProductosTop(Integer limit) {
        var items = itemPedidoRepository.findAll();

        Map<UUID, ProductoReporteResponse> productosMap = new HashMap<>();

        for (var item : items) {
            UUID productoId = item.getProducto().getId();
            String productoNombre = item.getProducto().getNombre();

            ProductoReporteResponse productoReporte = productosMap.computeIfAbsent(
                    productoId,
                    k -> ProductoReporteResponse.builder()
                            .productoId(productoId)
                            .productoNombre(productoNombre)
                            .cantidadVendida(0L)
                            .revenueTotal(0.0)
                            .build()
            );

            productoReporte.setCantidadVendida(productoReporte.getCantidadVendida() + item.getCantidad());
            productoReporte.setRevenueTotal(
                    productoReporte.getRevenueTotal() +
                    (item.getSubtotal() != null ? item.getSubtotal().doubleValue() : 0.0)
            );
        }

        return productosMap.values().stream()
                .sorted((a, b) -> b.getCantidadVendida().compareTo(a.getCantidadVendida()))
                .limit(limit)
                .toList();
    }

    /* ── Exportacion a Excel (.xlsx) ── */

    /** Genera el Excel del reporte de ventas para el rango de fechas dado. */
    public byte[] generarExcelVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null) fechaInicio = LocalDate.now().minusDays(30);
        if (fechaFin == null) fechaFin = LocalDate.now();

        var pedidos = pedidoRepository.findByFechaRangoYEstado(
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(LocalTime.MAX)
        );

        double totalVentas = pedidos.stream()
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal().doubleValue() : 0.0)
                .sum();
        long totalPedidos = pedidos.size();
        double promedio = totalPedidos > 0 ? totalVentas / totalPedidos : 0.0;

        XlsxWriter xlsx = new XlsxWriter("Ventas");
        xlsx.row("Reporte de Ventas - Cafeteria UTP");
        xlsx.row("Desde:", fechaInicio.toString(), "Hasta:", fechaFin.toString());
        xlsx.row("Total ventas (S/):", totalVentas);
        xlsx.row("Total pedidos:", totalPedidos);
        xlsx.row("Promedio por pedido (S/):", promedio);
        xlsx.row();
        xlsx.row("ID Pedido", "Cliente", "Codigo", "Fecha", "Estado", "Items", "Total (S/)");

        for (var p : pedidos) {
            int items = p.getItems() == null ? 0 : p.getItems().stream()
                    .mapToInt(i -> i.getCantidad() != null ? i.getCantidad() : 0).sum();
            xlsx.row(
                    p.getId() != null ? p.getId().toString() : "",
                    p.getUsuario() != null ? p.getUsuario().getNombre() : "",
                    p.getUsuario() != null ? p.getUsuario().getCodigo() : "",
                    p.getCreatedAt() != null ? p.getCreatedAt().format(FMT_FECHA) : "",
                    p.getEstado() != null ? p.getEstado().name() : "",
                    items,
                    p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO
            );
        }

        return xlsx.toBytes();
    }

    /** Genera el Excel con el inventario completo de productos. */
    public byte[] generarExcelInventario() {
        var productos = productoRepository.findAll();

        XlsxWriter xlsx = new XlsxWriter("Inventario");
        xlsx.row("Reporte de Inventario - Cafeteria UTP");
        xlsx.row("Total productos:", (long) productos.size());
        xlsx.row();
        xlsx.row("Producto", "Categoria", "Subcategoria", "Precio (S/)", "Stock", "Disponible");

        for (var p : productos) {
            xlsx.row(
                    p.getNombre(),
                    p.getCategoria() != null ? p.getCategoria().getNombre() : "",
                    p.getSubcategoria() != null ? p.getSubcategoria().getNombre() : "",
                    p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO,
                    p.getStock() != null ? p.getStock() : 0,
                    Boolean.TRUE.equals(p.getDisponible()) ? "Si" : "No"
            );
        }

        return xlsx.toBytes();
    }
}
