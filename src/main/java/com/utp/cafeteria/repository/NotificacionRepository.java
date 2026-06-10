package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);
    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaDesc(UUID usuarioId);
    long countByUsuarioIdAndLeidaFalse(UUID usuarioId);
}
