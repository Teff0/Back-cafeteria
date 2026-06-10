package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.ProductoRequest;
import com.utp.cafeteria.dto.ProductoResponse;
import com.utp.cafeteria.entity.Categoria;
import com.utp.cafeteria.entity.Producto;
import com.utp.cafeteria.entity.Subcategoria;
import com.utp.cafeteria.exception.ResourceNotFoundException;
import com.utp.cafeteria.repository.CategoriaRepository;
import com.utp.cafeteria.repository.ProductoRepository;
import com.utp.cafeteria.repository.SubcategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerDisponibles() {
        return productoRepository.findByDisponibleTrue().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerPorCategoria(UUID categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId).stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerPorSubcategoria(UUID subcategoriaId) {
        return productoRepository.findBySubcategoriaId(subcategoriaId).stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(UUID id) {
        return ProductoResponse.from(
                productoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id))
        );
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getCategoriaId()));

        Subcategoria subcategoria = null;
        if (request.getSubcategoriaId() != null) {
            subcategoria = subcategoriaRepository.findById(request.getSubcategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategoria", "id", request.getSubcategoriaId()));
        }

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .categoria(categoria)
                .subcategoria(subcategoria)
                .imagenUrl(request.getImagenUrl())
                .stock(request.getStock() != null ? request.getStock() : 100)
                .tiempoPreparacion(request.getTiempoPreparacion())
                .horarioDisponible(request.getHorarioDisponible())
                .build();
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(UUID id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getCategoriaId()));

        Subcategoria subcategoria = null;
        if (request.getSubcategoriaId() != null) {
            subcategoria = subcategoriaRepository.findById(request.getSubcategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategoria", "id", request.getSubcategoriaId()));
        }

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCategoria(categoria);
        producto.setSubcategoria(subcategoria);
        producto.setImagenUrl(request.getImagenUrl());
        producto.setTiempoPreparacion(request.getTiempoPreparacion());
        producto.setHorarioDisponible(request.getHorarioDisponible());
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
