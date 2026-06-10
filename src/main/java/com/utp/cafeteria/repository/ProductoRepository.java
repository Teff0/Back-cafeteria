package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Categoria;
import com.utp.cafeteria.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    Optional<Producto> findByNombre(String nombre);
    List<Producto> findByDisponibleTrue();
    List<Producto> findByDisponibleTrueAndCategoria(Categoria categoria);
    List<Producto> findByCategoriaId(UUID categoriaId);
    List<Producto> findBySubcategoriaId(UUID subcategoriaId);
}
