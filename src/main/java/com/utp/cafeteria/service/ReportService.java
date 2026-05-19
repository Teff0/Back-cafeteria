package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.ProductoReporteResponse;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public Map<String, Object> obtenerReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null) fechaInicio = LocalDate.now().minusDays(30);
        if (fechaFin == null) fechaFin = LocalDate.now();

        var pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getCreatedAt().toLocalDate().isAfter(fechaInicio.minusDays(1)))
                .filter(p -> p.getCreatedAt().toLocalDate().isBefore(fechaFin.plusDays(1)))
                .filter(p -> p.getEstado() != com.utp.cafeteria.entity.Pedido.Estado.CANCELADO)
                .toList();

        double totalVentas = pedidos.stream()
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0)
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
                    () -> ProductoReporteResponse.builder()
                            .productoId(productoId)
                            .productoNombre(productoNombre)
                            .cantidadVendida(0L)
                            .revenueTotal(0.0)
                            .build()
            );

            productoReporte.setCantidadVendida(productoReporte.getCantidadVendida() + item.getCantidad());
            productoReporte.setRevenueTotal(productoReporte.getRevenueTotal() + (item.getSubtotal() != null ? item.getSubtotal() : 0));
        }

        return productosMap.values().stream()
                .sorted((a, b) -> b.getCantidadVendida().compareTo(a.getCantidadVendida()))
                .limit(limit)
                .toList();
    }
}