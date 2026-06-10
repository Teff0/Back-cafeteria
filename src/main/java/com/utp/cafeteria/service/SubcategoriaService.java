package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.SubcategoriaRequest;
import com.utp.cafeteria.dto.SubcategoriaResponse;
import com.utp.cafeteria.entity.Categoria;
import com.utp.cafeteria.entity.Subcategoria;
import com.utp.cafeteria.exception.ResourceNotFoundException;
import com.utp.cafeteria.repository.CategoriaRepository;
import com.utp.cafeteria.repository.SubcategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<SubcategoriaResponse> obtenerPorCategoria(UUID categoriaId) {
        return subcategoriaRepository.findByCategoriaId(categoriaId).stream()
                .map(SubcategoriaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubcategoriaResponse> obtenerTodas() {
        return subcategoriaRepository.findAll().stream()
                .map(SubcategoriaResponse::from)
                .toList();
    }

    @Transactional
    public SubcategoriaResponse crear(SubcategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getCategoriaId()));
        Subcategoria sub = Subcategoria.builder()
                .categoria(categoria)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .imagenUrl(request.getImagenUrl())
                .build();
        return SubcategoriaResponse.from(subcategoriaRepository.save(sub));
    }

    @Transactional
    public SubcategoriaResponse actualizar(UUID id, SubcategoriaRequest request) {
        Subcategoria sub = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoria", "id", id));
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getCategoriaId()));
        sub.setCategoria(categoria);
        sub.setNombre(request.getNombre());
        sub.setDescripcion(request.getDescripcion());
        sub.setImagenUrl(request.getImagenUrl());
        return SubcategoriaResponse.from(subcategoriaRepository.save(sub));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!subcategoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subcategoria", "id", id);
        }
        subcategoriaRepository.deleteById(id);
    }
}
