package com.utp.cafeteria.repository;

import com.utp.cafeteria.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {
    List<Menu> findByActivoTrue();
    Optional<Menu> findByFechaAndActivoTrue(LocalDate fecha);
    List<Menu> findByFechaAndActivoTrueOrderByHorario(LocalDate fecha);
    Optional<Menu> findByFechaAndHorarioAndActivoTrue(LocalDate fecha, Menu.Horario horario);
}