package com.utp.cafeteria.controller;

import com.utp.cafeteria.controller.*;
import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.*;
import com.utp.cafeteria.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class AdminController {

    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final WebSocketNotificationService notificationService;

    @GetMapping("/pedidos")
    public ResponseEntity<List<PedidoResponse>> obtenerTodosLosPedidos() {
        return ResponseEntity.ok(pedidoService.obtenerTodosLosPedidos());
    }

    @GetMapping("/pedidos/encola")
    public ResponseEntity<List<PedidoResponse>> obtenerPedidosEnCola() {
        return ResponseEntity.ok(pedidoService.obtenerPedidosEnCola());
    }

    @PatchMapping("/pedidos/{id}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstadoPedido(
            @PathVariable UUID id,
            @RequestBody com.utp.cafeteria.dto.EstadoChangeRequest request,
            @RequestParam UUID adminId) {
        
        Pedido.Estado nuevoEstado = Pedido.Estado.valueOf(request.getNuevoEstado());
        PedidoResponse response = pedidoService.cambiarEstado(id, nuevoEstado, adminId);

        notificationService.notificarCambioEstado(id, nuevoEstado.name());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoResponse>> listarProductos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @PostMapping("/productos")
    public ResponseEntity<ProductoResponse> crearProducto(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.crear(producto));
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable UUID id,
            @RequestBody Producto producto) {
        
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable UUID id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}