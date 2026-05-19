package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, UUID> {
    List<CarritoItem> findByUsuarioId(UUID usuarioId);
    Optional<CarritoItem> findByUsuarioIdAndProductoId(UUID usuarioId, UUID productoId);
}