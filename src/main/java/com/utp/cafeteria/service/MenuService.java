package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.*;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final ProductoRepository productoRepository;

    public MenuResponse obtenerMenuDelDia() {
        LocalDate hoy = LocalDate.now();
        
        List<Menu> menus = menuRepository.findByFechaAndActivoTrueOrderByHorario(hoy);
        
        if (menus.isEmpty()) {
            throw new ResourceNotFoundException("Menú del día no encontrado");
        }

        Menu menu = menus.get(0);
        
        return MenuResponse.from(menu);
    }

    public List<MenuResponse> obtenerMenusDisponibles() {
        List<Menu> menus = menuRepository.findByActivoTrue();
        
        if (menus.isEmpty()) {
            throw new ResourceNotFoundException("No hay menús disponibles");
        }
        
        return menus.stream()
                .map(MenuResponse::from)
                .toList();
    }

    public List<MenuResponse> obtenerMenusPorFecha(LocalDate fecha) {
        List<Menu> menus = menuRepository.findByFechaAndActivoTrueOrderByHorario(fecha);
        
        if (menus.isEmpty()) {
            throw new ResourceNotFoundException("No hay menús para la fecha: " + fecha);
        }
        
        return menus.stream()
                .map(MenuResponse::from)
                .toList();
    }

    public MenuResponse obtenerMenu(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menú", "id", menuId));
        
        return MenuResponse.from(menu);
    }
}