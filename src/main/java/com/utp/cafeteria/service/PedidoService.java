package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.*;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request, UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        Menu menu = null;
        if (request.getMenuId() != null) {
            menu = menuRepository.findById(request.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menú", "id", request.getMenuId()));
        }

        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .menu(menu)
                .estado(Pedido.Estado.PENDIENTE)
                .horaProgramada(request.getHoraProgramada())
                .observaciones(request.getObservaciones())
                .build();

        for (ItemPedidoRequest itemRequest : request.getItems()) {
            Producto producto = productoRepository.findById(itemRequest.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", itemRequest.getProductoId()));

            if (!producto.getDisponible()) {
                throw new BadRequestException("Producto no disponible: " + producto.getNombre());
            }

            if (producto.getStock() < itemRequest.getCantidad()) {
                throw new BadRequestException("Stock insuficiente para: " + producto.getNombre());
            }

            ItemPedido item = ItemPedido.builder()
                    .producto(producto)
                    .cantidad(itemRequest.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .build();
            item.calcularSubtotal();

            pedido.agregarItem(item);

            producto.setStock(producto.getStock() - itemRequest.getCantidad());
            productoRepository.save(producto);
        }

        pedido.calcularTotal();
        pedidoRepository.save(pedido);

        return mapToResponse(pedido);
    }

    public List<PedidoResponse> obtenerMisPedidos(UUID usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioId(usuarioId);
        
        return pedidos.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PedidoResponse obtenerPedido(UUID pedidoId, UUID usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este pedido");
        }

        return mapToResponse(pedido);
    }

    public List<PedidoResponse> obtenerTodosLosPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        
        return pedidos.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<PedidoResponse> obtenerPedidosEnCola() {
        List<Pedido> pedidos = pedidoRepository.findAllEnCola();
        
        return pedidos.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PedidoResponse cambiarEstado(UUID pedidoId, Pedido.Estado nuevoEstado, UUID adminId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);

        return mapToResponse(pedido);
    }

    private PedidoResponse mapToResponse(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .usuarioEmail(pedido.getUsuario().getEmail())
                .usuarioNombre(pedido.getUsuario().getNombre())
                .menuId(pedido.getMenu() != null ? pedido.getMenu().getId() : null)
                .menuFecha(pedido.getMenu() != null ? pedido.getMenu().getFecha().toString() : null)
                .estado(pedido.getEstado())
                .horaProgramada(pedido.getHoraProgramada())
                .total(pedido.getTotal())
                .observaciones(pedido.getObservaciones())
                .items(pedido.getItems().stream()
                        .map(item -> PedidoResponse.ItemPedidoResponse.builder()
                                .id(item.getId())
                                .productoId(item.getProducto().getId())
                                .productoNombre(item.getProducto().getNombre())
                                .cantidad(item.getCantidad())
                                .precioUnitario(item.getPrecioUnitario())
                                .subtotal(item.getSubtotal())
                                .build())
                        .toList())
                .createdAt(pedido.getCreatedAt())
                .updatedAt(pedido.getUpdatedAt())
                .build();
    }
}