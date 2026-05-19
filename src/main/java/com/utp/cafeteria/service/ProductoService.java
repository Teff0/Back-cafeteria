package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.*;
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
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        
        return ProductoResponse.from(producto);
    }

    @Transactional
    public ProductoResponse crear(Producto producto) {
        producto.setDisponible(true);
        producto.setStock(100);
        
        Producto saved = productoRepository.save(producto);
        
        return ProductoResponse.from(saved);
    }

    @Transactional
    public ProductoResponse actualizar(UUID id, Producto productoActualizado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setCategoria(productoActualizado.getCategoria());
        producto.setDisponible(productoActualizado.getDisponible());
        producto.setImagenUrl(productoActualizado.getImagenUrl());
        
        Producto saved = productoRepository.save(producto);
        
        return ProductoResponse.from(saved);
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