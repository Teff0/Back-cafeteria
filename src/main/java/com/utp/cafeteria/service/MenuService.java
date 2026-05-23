package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.MenuRequest;
import com.utp.cafeteria.dto.MenuResponse;
import com.utp.cafeteria.entity.Menu;
import com.utp.cafeteria.entity.Producto;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            throw new ResourceNotFoundException("Menu del dia no encontrado");
        }
        return MenuResponse.from(menus.get(0));
    }

    public List<MenuResponse> obtenerMenusDisponibles() {
        List<Menu> menus = menuRepository.findByActivoTrue();
        if (menus.isEmpty()) {
            throw new ResourceNotFoundException("No hay menus disponibles");
        }
        return menus.stream().map(MenuResponse::from).toList();
    }

    public List<MenuResponse> obtenerMenusPorFecha(LocalDate fecha) {
        List<Menu> menus = menuRepository.findByFechaAndActivoTrueOrderByHorario(fecha);
        if (menus.isEmpty()) {
            throw new ResourceNotFoundException("No hay menus para la fecha: " + fecha);
        }
        return menus.stream().map(MenuResponse::from).toList();
    }

    public MenuResponse obtenerMenu(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", menuId));
        return MenuResponse.from(menu);
    }

    @Transactional
    public MenuResponse crearMenu(MenuRequest request) {
        if (menuRepository.findByFechaAndHorarioAndActivoTrue(request.getFecha(), request.getHorario()).isPresent()) {
            throw new ConflictException("Ya existe un menu activo para esa fecha y horario");
        }

        Set<Producto> productos = resolverProductos(request.getProductoIds());

        Menu menu = Menu.builder()
                .fecha(request.getFecha())
                .horario(request.getHorario())
                .activo(true)
                .productos(productos)
                .build();

        return MenuResponse.from(menuRepository.save(menu));
    }

    @Transactional
    public MenuResponse actualizarMenu(UUID id, MenuRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", id));

        menu.setFecha(request.getFecha());
        menu.setHorario(request.getHorario());

        if (request.getProductoIds() != null) {
            menu.setProductos(resolverProductos(request.getProductoIds()));
        }

        return MenuResponse.from(menuRepository.save(menu));
    }

    @Transactional
    public void eliminarMenu(UUID id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", id));
        menu.setActivo(false);
        menuRepository.save(menu);
    }

    private Set<Producto> resolverProductos(Set<UUID> productoIds) {
        if (productoIds == null || productoIds.isEmpty()) return new HashSet<>();
        Set<Producto> productos = new HashSet<>(productoRepository.findAllById(productoIds));
        if (productos.size() != productoIds.size()) {
            throw new ResourceNotFoundException("Uno o mas productos no fueron encontrados");
        }
        return productos;
    }
}
