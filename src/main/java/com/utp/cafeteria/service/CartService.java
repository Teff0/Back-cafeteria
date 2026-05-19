package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.*;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
                .map(this::mapToResponse)
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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

        if (producto.getStock() < request.getCantidad()) {
            throw new BadRequestException("Stock insuficiente para: " + producto.getNombre());
        }

        CarritoItem itemExistente = carritoItemRepository
                .findByUsuarioIdAndProductoId(usuarioId, request.getProductoId())
                .orElse(null);

        if (itemExistente != null) {
            int nuevaCantidad = itemExistente.getCantidad() + request.getCantidad();
            if (producto.getStock() < nuevaCantidad) {
                throw new BadRequestException("Stock insuficiente para: " + producto.getNombre());
            }
            itemExistente.setCantidad(nuevaCantidad);
            return mapToResponse(carritoItemRepository.save(itemExistente));
        }

        CarritoItem item = CarritoItem.builder()
                .usuario(usuario)
                .producto(producto)
                .cantidad(request.getCantidad())
                .build();

        return mapToResponse(carritoItemRepository.save(item));
    }

    @Transactional
    public CartItemResponse actualizarItem(UUID itemId, CartItemRequest request, UUID usuarioId) {
        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        if (!item.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este item");
        }

        if (item.getProducto().getStock() < request.getCantidad()) {
            throw new BadRequestException("Stock insuficiente para: " + item.getProducto().getNombre());
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
        BigDecimal precio = item.getProducto().getPrecio();
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));
        return CartItemResponse.builder()
                .id(item.getId())
                .productoId(item.getProducto().getId())
                .productoNombre(item.getProducto().getNombre())
                .cantidad(item.getCantidad())
                .precioUnitario(precio)
                .subtotal(subtotal)
                .build();
    }
}
