package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, UUID> {
    List<Resena> findByProductoIdOrderByFechaDesc(UUID productoId);
    Optional<Resena> findByUsuarioIdAndProductoId(UUID usuarioId, UUID productoId);

    @Query("SELECT AVG(r.calificacion) FROM Resena r WHERE r.producto.id = :productoId")
    Double promedioCalificacion(@Param("productoId") UUID productoId);
}
