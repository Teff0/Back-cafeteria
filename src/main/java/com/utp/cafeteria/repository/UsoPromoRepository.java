package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.UsoPromo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsoPromoRepository extends JpaRepository<UsoPromo, UUID> {
    boolean existsByDescuentoIdAndUsuarioId(UUID descuentoId, UUID usuarioId);
}
