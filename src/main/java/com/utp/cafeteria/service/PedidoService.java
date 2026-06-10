package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.ItemPedidoRequest;
import com.utp.cafeteria.dto.PedidoRequest;
import com.utp.cafeteria.dto.PedidoResponse;
import com.utp.cafeteria.entity.ItemPedido;
import com.utp.cafeteria.entity.Menu;
import com.utp.cafeteria.entity.Pedido;
import com.utp.cafeteria.entity.Producto;
import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.exception.BadRequestException;
import com.utp.cafeteria.exception.ResourceNotFoundException;
import com.utp.cafeteria.exception.UnauthorizedException;
import com.utp.cafeteria.repository.MenuRepository;
import com.utp.cafeteria.repository.PedidoRepository;
import com.utp.cafeteria.repository.ProductoRepository;
import com.utp.cafeteria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final Map<Pedido.Estado, Set<Pedido.Estado>> TRANSICIONES_VALIDAS = Map.of(
            Pedido.Estado.PAGADO,         EnumSet.of(Pedido.Estado.EN_PREPARACION, Pedido.Estado.CANCELADO),
            Pedido.Estado.EN_PREPARACION, EnumSet.of(Pedido.Estado.LISTO),
            Pedido.Estado.LISTO,          EnumSet.of(Pedido.Estado.ENTREGADO)
    );

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
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", request.getMenuId()));
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
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::mapToResponseUsuario)
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

    public PedidoResponse obtenerPedidoPorId(UUID pedidoId) {
        return mapToResponse(
                pedidoRepository.findById(pedidoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId))
        );
    }

    public List<PedidoResponse> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<PedidoResponse> obtenerPedidosEnCola() {
        return pedidoRepository.findAllEnCola().stream()
                .map(this::mapToResponseCaja)
                .toList();
    }

    @Transactional
    public PedidoResponse cambiarEstado(UUID pedidoId, Pedido.Estado nuevoEstado, UUID adminId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        Set<Pedido.Estado> permitidos = TRANSICIONES_VALIDAS.get(pedido.getEstado());
        if (permitidos == null || !permitidos.contains(nuevoEstado)) {
            throw new BadRequestException(
                    "Transicion invalida: " + pedido.getEstado() + " -> " + nuevoEstado
            );
        }

        pedido.setEstado(nuevoEstado);
        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse guardarVoucher(UUID pedidoId, String voucherUrl, UUID usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este pedido");
        }

        pedido.setVoucherUrl(voucherUrl);
        // Al subir el comprobante el pedido pasa a "pago en verificacion"
        if (pedido.getEstado() == Pedido.Estado.PENDIENTE) {
            pedido.setEstado(Pedido.Estado.PAGADO);
        }
        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cancelarPedido(UUID pedidoId, UUID usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este pedido");
        }

        if (pedido.getEstado() != Pedido.Estado.PENDIENTE) {
            throw new BadRequestException("Solo se pueden cancelar pedidos pendientes");
        }

        for (ItemPedido item : pedido.getItems()) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() + item.getCantidad());
            productoRepository.save(producto);
        }

        pedido.setEstado(Pedido.Estado.CANCELADO);
        return mapToResponse(pedidoRepository.save(pedido));
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
                .voucherUrl(pedido.getVoucherUrl())
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

    private PedidoResponse mapToResponseCaja(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .usuarioNombre(pedido.getUsuario().getNombre())
                .estado(pedido.getEstado())
                .total(pedido.getTotal())
                .voucherUrl(pedido.getVoucherUrl())
                .items(pedido.getItems().stream()
                        .map(item -> PedidoResponse.ItemPedidoResponse.builder()
                                .productoNombre(item.getProducto().getNombre())
                                .cantidad(item.getCantidad())
                                .subtotal(item.getSubtotal())
                                .build())
                        .toList())
                .createdAt(pedido.getCreatedAt())
                .build();
    }

    private PedidoResponse mapToResponseUsuario(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .estado(pedido.getEstado())
                .total(pedido.getTotal())
                .items(pedido.getItems().stream()
                        .map(item -> PedidoResponse.ItemPedidoResponse.builder()
                                .productoNombre(item.getProducto().getNombre())
                                .cantidad(item.getCantidad())
                                .subtotal(item.getSubtotal())
                                .build())
                        .toList())
                .createdAt(pedido.getCreatedAt())
                .build();
    }
}
