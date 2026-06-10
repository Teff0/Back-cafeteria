package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubcategoriaRepository extends JpaRepository<Subcategoria, UUID> {
    List<Subcategoria> findByCategoriaId(UUID categoriaId);
}
