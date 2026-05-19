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

    @Transactional
    public MenuResponse crearMenu(Menu menu) {
        menu.setActivo(true);
        return MenuResponse.from(menuRepository.save(menu));
    }

    @Transactional
    public MenuResponse actualizarMenu(UUID id, Menu menuActualizado) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menú", "id", id));
        
        menu.setNombre(menuActualizado.getNombre());
        menu.setDescripcion(menuActualizado.getDescripcion());
        menu.setPrecio(menuActualizado.getPrecio());
        menu.setFecha(menuActualizado.getFecha());
        menu.setHorario(menuActualizado.getHorario());
        
        return MenuResponse.from(menuRepository.save(menu));
    }

    @Transactional
    public void eliminarMenu(UUID id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menú", "id", id));
        menu.setActivo(false);
        menuRepository.save(menu);
    }
}