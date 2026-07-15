package com.utp.cafeteria.controller;

import com.utp.cafeteria.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Subida de imagenes (comprobantes de pago, fotos de productos).
 * Guarda el archivo en la carpeta local {@code uploads/} y devuelve su URL relativa.
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    @PostMapping
    public ResponseEntity<Map<String, String>> subir(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }
        try {
            Files.createDirectories(UPLOAD_DIR);
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "img";
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);
            String nombre = UUID.randomUUID() + ext;
            Path destino = UPLOAD_DIR.resolve(nombre).toAbsolutePath();
            file.transferTo(destino);
            return ResponseEntity.ok(Map.of("url", "/uploads/" + nombre));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
        }
    }
}
