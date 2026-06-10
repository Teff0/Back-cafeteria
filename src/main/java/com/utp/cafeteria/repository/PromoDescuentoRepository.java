package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.PromoDescuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromoDescuentoRepository extends JpaRepository<PromoDescuento, UUID> {
    Optional<PromoDescuento> findByCodigoPromo(String codigoPromo);
    List<PromoDescuento> findByActivoTrue();
}
