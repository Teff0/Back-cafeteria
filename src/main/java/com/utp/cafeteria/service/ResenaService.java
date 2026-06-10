package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.ResenaRequest;
import com.utp.cafeteria.dto.ResenaResponse;
import com.utp.cafeteria.entity.Producto;
import com.utp.cafeteria.entity.Resena;
import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.exception.ConflictException;
import com.utp.cafeteria.exception.ResourceNotFoundException;
import com.utp.cafeteria.exception.UnauthorizedException;
import com.utp.cafeteria.repository.ProductoRepository;
import com.utp.cafeteria.repository.ResenaRepository;
import com.utp.cafeteria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<ResenaResponse> obtenerPorProducto(UUID productoId) {
        return resenaRepository.findByProductoIdOrderByFechaDesc(productoId).stream()
                .map(ResenaResponse::from)
                .toList();
    }

    public Double promedioCalificacion(UUID productoId) {
        return resenaRepository.promedioCalificacion(productoId);
    }

    @Transactional
    public ResenaResponse crear(ResenaRequest request, UUID usuarioId) {
        if (resenaRepository.findByUsuarioIdAndProductoId(usuarioId, request.getProductoId()).isPresent()) {
            throw new ConflictException("Ya tienes una resena para este producto");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductoId()));

        Resena resena = Resena.builder()
                .usuario(usuario)
                .producto(producto)
                .calificacion(request.getCalificacion())
                .comentario(request.getComentario())
                .build();
        return ResenaResponse.from(resenaRepository.save(resena));
    }

    @Transactional
    public void eliminar(UUID id, UUID usuarioId) {
        Resena resena = resenaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resena", "id", id));
        if (!resena.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta resena");
        }
        resenaRepository.deleteById(id);
    }
}
