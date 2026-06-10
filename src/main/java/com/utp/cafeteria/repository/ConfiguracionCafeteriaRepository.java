package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.ConfiguracionCafeteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConfiguracionCafeteriaRepository extends JpaRepository<ConfiguracionCafeteria, UUID> {
}
