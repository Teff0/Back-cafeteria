package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    List<Producto> findByDisponibleTrue();
    List<Producto> findByCategoria(Producto.Categoria categoria);
    List<Producto> findByDisponibleTrueAndCategoria(Producto.Categoria categoria);
}