package com.utp.cafeteria.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generador minimo de archivos .xlsx (OOXML) usando solo el JDK, sin dependencias externas.
 * <p>
 * Soporta una hoja con celdas de texto o numericas (inline strings, sin sharedStrings).
 * Suficiente para reportes simples. Las celdas pueden ser String, Number o null.
 */
public class XlsxWriter {

    private final String sheetName;
    private final List<List<Object>> rows = new ArrayList<>();

    public XlsxWriter(String sheetName) {
        this.sheetName = (sheetName == null || sheetName.isBlank()) ? "Hoja1" : sheetName;
    }

    /** Agrega una fila. Cada celda puede ser String, Number (BigDecimal/Double/Integer...) o null. */
    public XlsxWriter row(Object... cells) {
        rows.add(Arrays.asList(cells));
        return this;
    }

    /** Construye el .xlsx completo (un ZIP de partes OOXML) en memoria. */
    public byte[] toBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(baos)) {
                put(zip, "[Content_Types].xml", contentTypes());
                put(zip, "_rels/.rels", rootRels());
                put(zip, "xl/workbook.xml", workbook());
                put(zip, "xl/_rels/workbook.xml.rels", workbookRels());
                put(zip, "xl/worksheets/sheet1.xml", sheet());
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el Excel", e);
        }
    }

    /* ── Partes del paquete OOXML ── */

    private void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
            + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
            + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
            + "</Types>";
    }

    private String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
            + "</Relationships>";
    }

    private String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
            + "<sheets><sheet name=\"" + escape(sheetName) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
            + "</workbook>";
    }

    private String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
            + "</Relationships>";
    }

    private String sheet() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
          .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        for (int r = 0; r < rows.size(); r++) {
            List<Object> cells = rows.get(r);
            int rowNum = r + 1;
            sb.append("<row r=\"").append(rowNum).append("\">");
            for (int c = 0; c < cells.size(); c++) {
                Object value = cells.get(c);
                String ref = colLetter(c) + rowNum;
                if (value instanceof Number) {
                    sb.append("<c r=\"").append(ref).append("\"><v>")
                      .append(numberStr((Number) value)).append("</v></c>");
                } else {
                    String text = value == null ? "" : value.toString();
                    sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                      .append(escape(text)).append("</t></is></c>");
                }
            }
            sb.append("</row>");
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    /* ── Soporte ── */

    private static String numberStr(Number n) {
        if (n instanceof BigDecimal) return ((BigDecimal) n).toPlainString();
        return n.toString();
    }

    /** Convierte un indice de columna (0->A, 25->Z, 26->AA...) a su letra Excel. */
    private static String colLetter(int index) {
        StringBuilder sb = new StringBuilder();
        int i = index;
        do {
            sb.insert(0, (char) ('A' + (i % 26)));
            i = i / 26 - 1;
        } while (i >= 0);
        return sb.toString();
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&':  out.append("&amp;");  break;
                case '<':  out.append("&lt;");   break;
                case '>':  out.append("&gt;");   break;
                case '"':  out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default:
                    // Omitir caracteres de control no validos en XML
                    if (ch >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') out.append(ch);
            }
        }
        return out.toString();
    }
}
