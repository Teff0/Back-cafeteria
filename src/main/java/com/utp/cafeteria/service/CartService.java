package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.*;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CarritoItemRepository carritoItemRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public CartResponse obtenerCarrito(UUID usuarioId) {
        List<CarritoItem> items = carritoItemRepository.findByUsuarioId(usuarioId);
        
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productoId(item.getProducto().getId())
                        .productoNombre(item.getProducto().getNombre())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getProducto().getPrecio())
                        .subtotal(item.getProducto().getPrecio() * item.getCantidad())
                        .build())
                .toList();
        
        double total = itemResponses.stream()
                .mapToDouble(CartItemResponse::getSubtotal)
                .sum();
        
        int cantidadTotal = items.stream()
                .mapToInt(CarritoItem::getCantidad)
                .sum();
        
        return CartResponse.builder()
                .items(itemResponses)
                .total(total)
                .cantidadTotal(cantidadTotal)
                .build();
    }

    @Transactional
    public CartItemResponse agregarItem(CartItemRequest request, UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));
        
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductoId()));
        
        if (!producto.getDisponible()) {
            throw new BadRequestException("Producto no disponible");
        }
        
        CarritoItem itemExistente = carritoItemRepository
                .findByUsuarioIdAndProductoId(usuarioId, request.getProductoId())
                .orElse(null);
        
        if (itemExistente != null) {
            itemExistente.setCantidad(itemExistente.getCantidad() + request.getCantidad());
            itemExistente = carritoItemRepository.save(itemExistente);
            return mapToResponse(itemExistente);
        }
        
        CarritoItem item = CarritoItem.builder()
                .usuario(usuario)
                .producto(producto)
                .cantidad(request.getCantidad())
                .build();
        
        item = carritoItemRepository.save(item);
        
        return mapToResponse(item);
    }

    @Transactional
    public CartItemResponse actualizarItem(UUID itemId, CartItemRequest request, UUID usuarioId) {
        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));
        
        if (!item.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este item");
        }
        
        item.setCantidad(request.getCantidad());
        
        return mapToResponse(carritoItemRepository.save(item));
    }

    @Transactional
    public void eliminarItem(UUID itemId, UUID usuarioId) {
        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));
        
        if (!item.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este item");
        }
        
        carritoItemRepository.delete(item);
    }

    @Transactional
    public void vaciarCarrito(UUID usuarioId) {
        List<CarritoItem> items = carritoItemRepository.findByUsuarioId(usuarioId);
        carritoItemRepository.deleteAll(items);
    }

    private CartItemResponse mapToResponse(CarritoItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productoId(item.getProducto().getId())
                .productoNombre(item.getProducto().getNombre())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getProducto().getPrecio())
                .subtotal(item.getProducto().getPrecio() * item.getCantidad())
                .build();
    }
}