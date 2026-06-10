package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.SubcategoriaRequest;
import com.utp.cafeteria.dto.SubcategoriaResponse;
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

    public List<SubcategoriaResponse> obtenerTodas() {
        return subcategoriaRepository.findAll().stream()
                .map(SubcategoriaResponse::from)
                .toList();
    }

    public List<SubcategoriaResponse> obtenerPorCategoria(UUID categoriaId) {
        return subcategoriaRepository.findByCategoriaId(categoriaId).stream()
                .map(SubcategoriaResponse::from)
                .toList();
    }

    @Transactional
    public SubcategoriaResponse crear(SubcategoriaRequest request) {
        if (!categoriaRepository.existsById(request.getCategoriaId())) {
            throw new ResourceNotFoundException("Categoria", "id", request.getCategoriaId());
        }
        Subcategoria s = Subcategoria.builder()
                .categoriaId(request.getCategoriaId())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
        return SubcategoriaResponse.from(subcategoriaRepository.save(s));
    }

    @Transactional
    public SubcategoriaResponse actualizar(UUID id, SubcategoriaRequest request) {
        Subcategoria s = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoria", "id", id));
        s.setCategoriaId(request.getCategoriaId());
        s.setNombre(request.getNombre());
        s.setDescripcion(request.getDescripcion());
        return SubcategoriaResponse.from(subcategoriaRepository.save(s));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!subcategoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subcategoria", "id", id);
        }
        subcategoriaRepository.deleteById(id);
    }
}
