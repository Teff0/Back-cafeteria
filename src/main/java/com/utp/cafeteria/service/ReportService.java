package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.InventarioResponse;
import com.utp.cafeteria.dto.ProductoReporteResponse;
import com.utp.cafeteria.dto.VentaDetalleResponse;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProductoRepository productoRepository;

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

    /**
     * Detalle de ventas por producto dentro del rango de fechas: cuanto se vendio
     * de cada producto, su categoria, subcategoria, costo unitario y total.
     */
    @Transactional(readOnly = true)
    public List<VentaDetalleResponse> obtenerDetalleVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null) fechaInicio = LocalDate.now().minusDays(30);
        if (fechaFin == null) fechaFin = LocalDate.now();

        var pedidos = pedidoRepository.findByFechaRangoYEstado(
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(LocalTime.MAX)
        );

        Map<UUID, VentaDetalleResponse> mapa = new LinkedHashMap<>();
        for (var pedido : pedidos) {
            for (var item : pedido.getItems()) {
                var prod = item.getProducto();
                var detalle = mapa.computeIfAbsent(prod.getId(), k -> VentaDetalleResponse.builder()
                        .producto(prod.getNombre())
                        .categoria(prod.getCategoria() != null ? prod.getCategoria().getNombre() : "—")
                        .subcategoria(prod.getSubcategoria() != null ? prod.getSubcategoria().getNombre() : "—")
                        .cantidadVendida(0L)
                        .costoUnitario(item.getPrecioUnitario())
                        .totalVendido(BigDecimal.ZERO)
                        .build());

                detalle.setCantidadVendida(detalle.getCantidadVendida() + item.getCantidad());
                detalle.setTotalVendido(detalle.getTotalVendido()
                        .add(item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO));
            }
        }

        return mapa.values().stream()
                .sorted((a, b) -> b.getCantidadVendida().compareTo(a.getCantidadVendida()))
                .toList();
    }

    /**
     * Inventario completo: todos los productos con su categoria, subcategoria,
     * precio, stock disponible y si estan activos.
     */
    @Transactional(readOnly = true)
    public List<InventarioResponse> obtenerInventario() {
        return productoRepository.findAll().stream()
                .map(p -> InventarioResponse.builder()
                        .producto(p.getNombre())
                        .categoria(p.getCategoria() != null ? p.getCategoria().getNombre() : "—")
                        .subcategoria(p.getSubcategoria() != null ? p.getSubcategoria().getNombre() : "—")
                        .precio(p.getPrecio())
                        .stock(p.getStock())
                        .disponible(p.getDisponible())
                        .build())
                .sorted(Comparator.comparing(InventarioResponse::getCategoria, Comparator.nullsLast(String::compareTo))
                        .thenComparing(InventarioResponse::getProducto, Comparator.nullsLast(String::compareTo)))
                .toList();
    }
}
