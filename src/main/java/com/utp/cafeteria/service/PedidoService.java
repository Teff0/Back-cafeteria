package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.ItemPedidoRequest;
import com.utp.cafeteria.dto.PedidoRequest;
import com.utp.cafeteria.dto.PedidoResponse;
import com.utp.cafeteria.entity.*;
import com.utp.cafeteria.exception.BadRequestException;
import com.utp.cafeteria.exception.ResourceNotFoundException;
import com.utp.cafeteria.exception.UnauthorizedException;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final Map<Pedido.Estado, Set<Pedido.Estado>> TRANSICIONES_VALIDAS = Map.of(
            Pedido.Estado.PENDIENTE,      EnumSet.of(Pedido.Estado.PAGADO, Pedido.Estado.CANCELADO),
            Pedido.Estado.PAGADO,         EnumSet.of(Pedido.Estado.EN_PREPARACION, Pedido.Estado.CANCELADO),
            Pedido.Estado.EN_PREPARACION, EnumSet.of(Pedido.Estado.LISTO),
            Pedido.Estado.LISTO,          EnumSet.of(Pedido.Estado.ENTREGADO)
    );

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MenuRepository menuRepository;
    private final PromoDescuentoRepository promoRepository;
    private final HistorialEstadoRepository historialRepository;
    private final UsoPromoRepository usoPromoRepository;
    private final PagoRepository pagoRepository;
    private final NotificacionService notificacionService;
    private final WebSocketNotificationService wsNotificationService;

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
                .metodoPago(request.getMetodoPago())
                .horaProgramada(request.getHoraProgramada() != null ? request.getHoraProgramada() : LocalTime.now())
                .observaciones(request.getObservaciones())
                .numeroPedido(generarNumeroPedido())
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
                    .nota(itemRequest.getNota())
                    .build();
            item.calcularSubtotal();
            pedido.agregarItem(item);

            producto.setStock(producto.getStock() - itemRequest.getCantidad());
            productoRepository.save(producto);
        }

        pedido.calcularTotal();

        if (request.getCodigoPromo() != null && !request.getCodigoPromo().isBlank()) {
            aplicarPromo(pedido, request.getCodigoPromo(), usuario);
        }

        pedidoRepository.save(pedido);
        registrarHistorial(pedido, Pedido.Estado.PENDIENTE, null);
        notificacionService.crearNotificacion(usuario, pedido,
                Notificacion.TipoNotificacion.PEDIDO, "Tu pedido #" + pedido.getNumeroPedido() + " fue creado.");
        wsNotificationService.notificarPedidoCreado(pedido.getId(), usuario.getEmail());

        return mapToResponse(pedido);
    }

    @Transactional
    public PedidoResponse subirVoucher(UUID pedidoId, String voucherUrl, UUID usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));
        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este pedido");
        }
        if (pedido.getEstado() != Pedido.Estado.PENDIENTE) {
            throw new BadRequestException("El pedido debe estar en estado PENDIENTE para subir comprobante");
        }
        pedido.setVoucherUrl(voucherUrl);
        pedido.setEstado(Pedido.Estado.PAGADO);
        pedidoRepository.save(pedido);
        registrarHistorial(pedido, Pedido.Estado.PAGADO, "Comprobante subido por el estudiante");
        notificacionService.crearNotificacion(pedido.getUsuario(), pedido,
                Notificacion.TipoNotificacion.PEDIDO,
                "Tu pedido #" + pedido.getNumeroPedido() + " - pago enviado, en verificación.");
        wsNotificationService.notificarCambioEstado(pedido.getId(), "PAGADO");
        return mapToResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerMisPedidos(UUID usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::mapToResponseUsuario)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(UUID pedidoId, UUID usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));
        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este pedido");
        }
        return mapToResponse(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedidoPorId(UUID pedidoId) {
        return mapToResponse(
                pedidoRepository.findById(pedidoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId))
        );
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
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
            throw new BadRequestException("Transicion invalida: " + pedido.getEstado() + " -> " + nuevoEstado);
        }

        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);
        registrarHistorial(pedido, nuevoEstado, null);
        notificacionService.crearNotificacion(pedido.getUsuario(), pedido,
                Notificacion.TipoNotificacion.PEDIDO,
                "Tu pedido #" + pedido.getNumeroPedido() + " cambio a: " + nuevoEstado.name());
        wsNotificationService.notificarCambioEstado(pedido.getId(), nuevoEstado.name());
        if (nuevoEstado == Pedido.Estado.LISTO) {
            wsNotificationService.notificarPedidoListo(pedido.getId());
        }

        return mapToResponseCaja(pedido);
    }

    @Transactional
    public PedidoResponse cancelarPedido(UUID pedidoId, UUID usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este pedido");
        }

        Pedido.Estado estadoActual = pedido.getEstado();
        // Se permite cancelar mientras el pedido aun no entra a cocina:
        // PENDIENTE (sin pagar) o PAGADO (con devolucion del dinero).
        if (estadoActual != Pedido.Estado.PENDIENTE && estadoActual != Pedido.Estado.PAGADO) {
            throw new BadRequestException(
                    "Solo puedes cancelar pedidos pendientes o pagados; este ya esta en preparacion o finalizado");
        }

        // Devolver el stock reservado por el pedido
        for (ItemPedido item : pedido.getItems()) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() + item.getCantidad());
            productoRepository.save(producto);
        }

        boolean huboPago = estadoActual == Pedido.Estado.PAGADO;
        String nota = huboPago
                ? "Cancelado por el usuario - reembolso de S/ " + pedido.getTotal()
                : "Cancelado por el usuario";

        pedido.setEstado(Pedido.Estado.CANCELADO);
        pedido.setMotivoCancelacion(nota);
        pedidoRepository.save(pedido);
        registrarHistorial(pedido, Pedido.Estado.CANCELADO, nota);

        if (huboPago) {
            // Marcar el pago como reembolsado y avisar de la devolucion
            pagoRepository.findByPedidoId(pedido.getId()).ifPresent(pago -> {
                pago.setEstado(Pago.EstadoPago.REEMBOLSADO);
                pagoRepository.save(pago);
            });
            notificacionService.crearNotificacion(pedido.getUsuario(), pedido,
                    Notificacion.TipoNotificacion.PAGO,
                    "Tu pedido #" + pedido.getNumeroPedido()
                            + " fue cancelado. Se procesara la devolucion de S/ " + pedido.getTotal() + ".");
        } else {
            notificacionService.crearNotificacion(pedido.getUsuario(), pedido,
                    Notificacion.TipoNotificacion.PEDIDO,
                    "Tu pedido #" + pedido.getNumeroPedido() + " fue cancelado.");
        }
        wsNotificationService.notificarCambioEstado(pedido.getId(), "CANCELADO");

        return mapToResponse(pedido);
    }

    private void aplicarPromo(Pedido pedido, String codigoPromo, Usuario usuario) {
        PromoDescuento promo = promoRepository.findByCodigoPromo(codigoPromo.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Codigo de promo invalido"));
        if (!promo.esValido()) {
            throw new BadRequestException("El codigo de promo no es valido o ya expiro");
        }
        if (usoPromoRepository.existsByDescuentoIdAndUsuarioId(promo.getId(), usuario.getId())) {
            throw new BadRequestException("Ya usaste este codigo de promo");
        }

        BigDecimal descuento;
        if (promo.getTipoDescuento() == PromoDescuento.TipoDescuento.PORCENTAJE) {
            descuento = pedido.getSubtotal().multiply(promo.getValor()).divide(BigDecimal.valueOf(100));
        } else {
            descuento = promo.getValor().min(pedido.getSubtotal());
        }

        pedido.setDescuento(promo);
        pedido.setDescuentoAplicado(descuento);
        pedido.setTotal(pedido.getSubtotal().subtract(descuento));

        promo.setUsosActuales(promo.getUsosActuales() + 1);
        promoRepository.save(promo);

        UsoPromo uso = UsoPromo.builder()
                .descuento(promo)
                .usuario(usuario)
                .pedido(pedido)
                .build();
        usoPromoRepository.save(uso);
    }

    private void registrarHistorial(Pedido pedido, Pedido.Estado estado, String nota) {
        HistorialEstado h = HistorialEstado.builder()
                .pedido(pedido)
                .estado(estado)
                .nota(nota)
                .build();
        historialRepository.save(h);
    }

    private String generarNumeroPedido() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PED-" + timestamp + "-" + (int)(Math.random() * 1000);
    }

    private PedidoResponse mapToResponse(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .usuarioEmail(pedido.getUsuario().getEmail())
                .usuarioNombre(pedido.getUsuario().getNombre())
                .menuId(pedido.getMenu() != null ? pedido.getMenu().getId() : null)
                .menuFecha(pedido.getMenu() != null ? pedido.getMenu().getFecha().toString() : null)
                .codigoPromo(pedido.getDescuento() != null ? pedido.getDescuento().getCodigoPromo() : null)
                .estado(pedido.getEstado())
                .metodoPago(pedido.getMetodoPago())
                .voucherUrl(pedido.getVoucherUrl())
                .horaProgramada(pedido.getHoraProgramada())
                .subtotal(pedido.getSubtotal())
                .descuentoAplicado(pedido.getDescuentoAplicado())
                .total(pedido.getTotal())
                .observaciones(pedido.getObservaciones())
                .motivoCancelacion(pedido.getMotivoCancelacion())
                .tiempoEstimado(pedido.getTiempoEstimado())
                .items(pedido.getItems().stream()
                        .map(item -> PedidoResponse.ItemPedidoResponse.builder()
                                .id(item.getId())
                                .productoId(item.getProducto().getId())
                                .productoNombre(item.getProducto().getNombre())
                                .cantidad(item.getCantidad())
                                .precioUnitario(item.getPrecioUnitario())
                                .subtotal(item.getSubtotal())
                                .nota(item.getNota())
                                .build())
                        .toList())
                .createdAt(pedido.getCreatedAt())
                .updatedAt(pedido.getUpdatedAt())
                .build();
    }

    private PedidoResponse mapToResponseCaja(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .usuarioNombre(pedido.getUsuario().getNombre())
                .estado(pedido.getEstado())
                .metodoPago(pedido.getMetodoPago())
                .voucherUrl(pedido.getVoucherUrl())
                .horaProgramada(pedido.getHoraProgramada())
                .total(pedido.getTotal())
                .tiempoEstimado(pedido.getTiempoEstimado())
                .items(pedido.getItems().stream()
                        .map(item -> PedidoResponse.ItemPedidoResponse.builder()
                                .id(item.getId())
                                .productoNombre(item.getProducto().getNombre())
                                .cantidad(item.getCantidad())
                                .subtotal(item.getSubtotal())
                                .nota(item.getNota())
                                .build())
                        .toList())
                .createdAt(pedido.getCreatedAt())
                .build();
    }

    private PedidoResponse mapToResponseUsuario(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .estado(pedido.getEstado())
                .metodoPago(pedido.getMetodoPago())
                .subtotal(pedido.getSubtotal())
                .descuentoAplicado(pedido.getDescuentoAplicado())
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
