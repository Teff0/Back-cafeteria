package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagoRepository extends JpaRepository<Pago, UUID> {
    Optional<Pago> findByPedidoId(UUID pedidoId);
    boolean existsByPedidoId(UUID pedidoId);
}