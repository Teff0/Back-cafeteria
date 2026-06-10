package com.utp.cafeteria.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.utp.cafeteria.dto.InventarioResponse;
import com.utp.cafeteria.dto.ProductoReporteResponse;
import com.utp.cafeteria.dto.VentaDetalleResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

/**
 * Genera reportes en formato Excel (.xlsx) con Apache POI.
 *
 * Seguridad: todo texto de origen externo (ej. nombre de producto) se sanitiza
 * con {@link #escribirTexto} para prevenir inyeccion de formulas (CSV/Formula
 * injection): un valor que comience con = + - @ seria interpretado como formula
 * por Excel. Se le antepone un apostrofo para forzar su lectura como texto.
 */
@Service
public class ExcelExportService {

    public byte[] exportarReporteVentas(Map<String, Object> reporte) {
        return exportarReporteVentas(reporte, List.of());
    }

    /**
     * Excel de ventas con dos hojas: "Resumen" (metricas) y "Detalle de ventas"
     * (cada producto vendido con categoria, subcategoria, cantidad y costos).
     */
    public byte[] exportarReporteVentas(Map<String, Object> reporte, List<VentaDetalleResponse> detalle) {
        Preconditions.checkNotNull(reporte, "El reporte de ventas no puede ser nulo");
        Preconditions.checkNotNull(detalle, "El detalle de ventas no puede ser nulo");

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = estiloEncabezado(workbook);

            // ── Hoja 1: Resumen ──
            Sheet resumen = workbook.createSheet("Resumen");
            Row header = resumen.createRow(0);
            escribirTexto(header.createCell(0), "Métrica", headerStyle);
            escribirTexto(header.createCell(1), "Valor", headerStyle);

            int fila = 1;
            for (Map.Entry<String, Object> entry : reporte.entrySet()) {
                Row row = resumen.createRow(fila++);
                escribirTexto(row.createCell(0), entry.getKey(), null);
                escribirValor(row.createCell(1), entry.getValue());
            }
            resumen.autoSizeColumn(0);
            resumen.autoSizeColumn(1);

            // ── Hoja 2: Detalle de ventas ──
            Sheet hoja = workbook.createSheet("Detalle de ventas");
            Row h = hoja.createRow(0);
            escribirTexto(h.createCell(0), "Producto", headerStyle);
            escribirTexto(h.createCell(1), "Categoría", headerStyle);
            escribirTexto(h.createCell(2), "Subcategoría", headerStyle);
            escribirTexto(h.createCell(3), "Cantidad vendida", headerStyle);
            escribirTexto(h.createCell(4), "Costo unitario (S/)", headerStyle);
            escribirTexto(h.createCell(5), "Total vendido (S/)", headerStyle);

            int f = 1;
            for (VentaDetalleResponse d : detalle) {
                Row row = hoja.createRow(f++);
                escribirTexto(row.createCell(0), d.getProducto(), null);
                escribirTexto(row.createCell(1), d.getCategoria(), null);
                escribirTexto(row.createCell(2), d.getSubcategoria(), null);
                escribirValor(row.createCell(3), d.getCantidadVendida());
                escribirValor(row.createCell(4), d.getCostoUnitario());
                escribirValor(row.createCell(5), d.getTotalVendido());
            }
            for (int i = 0; i <= 5; i++) hoja.autoSizeColumn(i);

            return toBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException("Error al generar el Excel de ventas", e);
        }
    }

    /**
     * Excel del inventario: todos los productos con categoria, subcategoria,
     * precio, stock y disponibilidad.
     */
    public byte[] exportarInventario(List<InventarioResponse> productos) {
        Preconditions.checkNotNull(productos, "La lista de inventario no puede ser nula");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventario");
            CellStyle headerStyle = estiloEncabezado(workbook);

            Row header = sheet.createRow(0);
            escribirTexto(header.createCell(0), "Producto", headerStyle);
            escribirTexto(header.createCell(1), "Categoría", headerStyle);
            escribirTexto(header.createCell(2), "Subcategoría", headerStyle);
            escribirTexto(header.createCell(3), "Precio (S/)", headerStyle);
            escribirTexto(header.createCell(4), "Stock", headerStyle);
            escribirTexto(header.createCell(5), "Disponible", headerStyle);

            int fila = 1;
            for (InventarioResponse p : productos) {
                Row row = sheet.createRow(fila++);
                escribirTexto(row.createCell(0), p.getProducto(), null);
                escribirTexto(row.createCell(1), p.getCategoria(), null);
                escribirTexto(row.createCell(2), p.getSubcategoria(), null);
                escribirValor(row.createCell(3), p.getPrecio());
                escribirValor(row.createCell(4), p.getStock());
                escribirTexto(row.createCell(5), Boolean.TRUE.equals(p.getDisponible()) ? "Sí" : "No", null);
            }
            for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);

            return toBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException("Error al generar el Excel de inventario", e);
        }
    }

    public byte[] exportarProductosTop(List<ProductoReporteResponse> productos) {
        Preconditions.checkNotNull(productos, "La lista de productos no puede ser nula");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos Top");
            CellStyle headerStyle = estiloEncabezado(workbook);

            Row header = sheet.createRow(0);
            escribirTexto(header.createCell(0), "Producto", headerStyle);
            escribirTexto(header.createCell(1), "Cantidad vendida", headerStyle);
            escribirTexto(header.createCell(2), "Revenue total", headerStyle);

            int fila = 1;
            for (ProductoReporteResponse p : productos) {
                Row row = sheet.createRow(fila++);
                escribirTexto(row.createCell(0), p.getProductoNombre(), null);
                escribirValor(row.createCell(1), p.getCantidadVendida());
                escribirValor(row.createCell(2), p.getRevenueTotal());
            }

            for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
            return toBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException("Error al generar el Excel de productos top", e);
        }
    }

    // ---- helpers ----

    private CellStyle estiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void escribirTexto(Cell cell, String valor, CellStyle style) {
        cell.setCellValue(sanitizar(valor));
        if (style != null) cell.setCellStyle(style);
    }

    private void escribirValor(Cell cell, Object valor) {
        if (valor instanceof Number numero) {
            cell.setCellValue(numero.doubleValue());
        } else {
            cell.setCellValue(sanitizar(valor == null ? "" : valor.toString()));
        }
    }

    /**
     * Neutraliza la inyeccion de formulas anteponiendo un apostrofo cuando el
     * texto empieza por un caracter que Excel trataria como inicio de formula.
     */
    String sanitizar(String valor) {
        if (Strings.isNullOrEmpty(valor)) {
            return "";
        }
        char primero = valor.charAt(0);
        if (primero == '=' || primero == '+' || primero == '-' || primero == '@'
                || primero == '\t' || primero == '\r') {
            return "'" + valor;
        }
        return valor;
    }

    private byte[] toBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
