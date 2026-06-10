package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.DisponibilidadProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisponibilidadProductoRepository extends JpaRepository<DisponibilidadProducto, UUID> {
    List<DisponibilidadProducto> findByFechaAndActivoTrue(LocalDate fecha);
    Optional<DisponibilidadProducto> findByProductoIdAndFecha(UUID productoId, LocalDate fecha);
}
