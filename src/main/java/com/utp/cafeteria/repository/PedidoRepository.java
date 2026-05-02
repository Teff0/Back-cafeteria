package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    
    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId ORDER BY p.createdAt DESC")
    List<Pedido> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
    
    @Query("SELECT p FROM Pedido p WHERE p.estado = :estado ORDER BY p.createdAt DESC")
    List<Pedido> findByEstado(Pedido.Estado estado);
    
    @Query("SELECT p FROM Pedido p WHERE p.estado IN :estados ORDER BY p.createdAt ASC")
    List<Pedido> findByEstadoIn(@Param("estados") List<Pedido.Estado> estados);
    
    @Query("SELECT p FROM Pedido p WHERE p.estado != 'CANCELADO' AND p.estado != 'ENTREGADO' ORDER BY p.createdAt ASC")
    List<Pedido> findAllEnCola();
    
    long countByEstado(Pedido.Estado estado);
}