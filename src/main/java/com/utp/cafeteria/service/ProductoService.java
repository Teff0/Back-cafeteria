package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.ProductoRequest;
import com.utp.cafeteria.dto.ProductoResponse;
import com.utp.cafeteria.entity.Producto;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    public List<ProductoResponse> obtenerDisponibles() {
        return productoRepository.findByDisponibleTrue().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    public List<ProductoResponse> obtenerPorCategoria(Producto.Categoria categoria) {
        return productoRepository.findByDisponibleTrueAndCategoria(categoria).stream()
                .map(ProductoResponse::from)
                .toList();
    }

    public ProductoResponse obtenerPorId(UUID id) {
        return ProductoResponse.from(
                productoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id))
        );
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .categoria(request.getCategoria())
                .imagenUrl(request.getImagenUrl())
                .disponible(true)
                .stock(request.getStock() != null ? request.getStock() : 100)
                .build();
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(UUID id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCategoria(request.getCategoria());
        producto.setImagenUrl(request.getImagenUrl());
        if (request.getStock() != null) {
            producto.setStock(request.getStock());
        }

        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto", "id", id);
        }
        productoRepository.deleteById(id);
    }

    @Transactional
    public ProductoResponse cambiarDisponibilidad(UUID id, Boolean disponible) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        producto.setDisponible(disponible);
        return ProductoResponse.from(productoRepository.save(producto));
    }
}
