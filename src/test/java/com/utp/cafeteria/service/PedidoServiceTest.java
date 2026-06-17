package com.utp.cafeteria.service;

import com.utp.cafeteria.entity.ItemPedido;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService — cancelarPedido (con Mockito, sin MySQL)")
class PedidoServiceTest {

    @Mock private PedidoRepository    pedidoRepository;
    @Mock private ProductoRepository  productoRepository;
    @Mock private UsuarioRepository   usuarioRepository;
    @Mock private MenuRepository      menuRepository;

    private PedidoService pedidoService;

    private UUID usuarioId;
    private UUID pedidoId;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepository, productoRepository, usuarioRepository, menuRepository);

        usuarioId = UUID.randomUUID();
        pedidoId  = UUID.randomUUID();

        usuario = Usuario.builder()
                .id(usuarioId)
                .email("cliente@utp.edu.pe")
                .nombre("Estudiante Test")
                .rol(Usuario.Rol.USUARIO)
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Pedido pedidoEnEstado(Pedido.Estado estado) {
        Producto producto = Producto.builder()
                .id(UUID.randomUUID())
                .nombre("Menú del día")
                .precio(BigDecimal.valueOf(8.50))
                .stock(10)
                .build();

        ItemPedido item = ItemPedido.builder()
                .producto(producto)
                .cantidad(2)
                .precioUnitario(BigDecimal.valueOf(8.50))
                .subtotal(BigDecimal.valueOf(17.00))
                .build();

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .usuario(usuario)
                .estado(estado)
                .total(BigDecimal.valueOf(17.00))
                .build();
        pedido.agregarItem(item);
        return pedido;
    }

    // ── casos positivos ───────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelarPedido: estado PENDIENTE → pedido queda CANCELADO y devuelve el response")
    void cancelarPedido_estadoPendiente_devuelvePedidoCancelado() {
        Pedido pedido = pedidoEnEstado(Pedido.Estado.PENDIENTE);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = pedidoService.cancelarPedido(pedidoId, usuarioId);

        assertAll("cancelación desde PENDIENTE",
                () -> assertEquals(Pedido.Estado.CANCELADO, pedido.getEstado()),
                () -> assertNotNull(response),
                () -> verify(pedidoRepository).save(pedido)
        );
    }

    @Test
    @DisplayName("cancelarPedido: estado PAGADO (comprobante subido) → pedido queda CANCELADO")
    void cancelarPedido_estadoPagado_devuelvePedidoCancelado() {
        Pedido pedido = pedidoEnEstado(Pedido.Estado.PAGADO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = pedidoService.cancelarPedido(pedidoId, usuarioId);

        assertAll("cancelación desde PAGADO",
                () -> assertEquals(Pedido.Estado.CANCELADO, pedido.getEstado()),
                () -> assertNotNull(response),
                () -> verify(productoRepository, atLeastOnce()).save(any(Producto.class))
        );
    }

    @Test
    @DisplayName("cancelarPedido: se restaura el stock de todos los items al cancelar")
    void cancelarPedido_estadoPendiente_restauraStock() {
        Pedido pedido = pedidoEnEstado(Pedido.Estado.PENDIENTE);
        int stockOriginal = pedido.getItems().get(0).getProducto().getStock();

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.cancelarPedido(pedidoId, usuarioId);

        int stockRestaurado = pedido.getItems().get(0).getProducto().getStock();
        assertEquals(stockOriginal + 2, stockRestaurado,
                "el stock debe aumentar en la cantidad del item cancelado");
    }

    // ── casos negativos: estado no cancelable ─────────────────────────────────

    @ParameterizedTest(name = "estado {0} → BadRequestException")
    @EnumSource(value = Pedido.Estado.class,
                names = {"EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"})
    @DisplayName("cancelarPedido: estados no cancelables lanzan BadRequestException")
    void cancelarPedido_estadoNoCancelable_lanzaBadRequestException(Pedido.Estado estado) {
        Pedido pedido = pedidoEnEstado(estado);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        assertThrows(BadRequestException.class,
                () -> pedidoService.cancelarPedido(pedidoId, usuarioId));

        verify(pedidoRepository, never()).save(any());
    }

    // ── casos negativos: acceso y existencia ─────────────────────────────────

    @Test
    @DisplayName("cancelarPedido: usuario distinto al dueño → UnauthorizedException")
    void cancelarPedido_usuarioDistinto_lanzaUnauthorizedException() {
        UUID otroUsuarioId = UUID.randomUUID();
        Pedido pedido = pedidoEnEstado(Pedido.Estado.PENDIENTE);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        assertThrows(UnauthorizedException.class,
                () -> pedidoService.cancelarPedido(pedidoId, otroUsuarioId));

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelarPedido: pedido inexistente → ResourceNotFoundException")
    void cancelarPedido_pedidoNoExiste_lanzaResourceNotFoundException() {
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> pedidoService.cancelarPedido(pedidoId, usuarioId));

        verify(pedidoRepository, never()).save(any());
    }

    // ── cambiarEstado: transiciones válidas e inválidas ───────────────────────

    @Test
    @DisplayName("cambiarEstado: PAGADO → EN_PREPARACION es una transición válida")
    void cambiarEstado_pagadoAEnPreparacion_exitoso() {
        Pedido pedido = pedidoEnEstado(Pedido.Estado.PAGADO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = pedidoService.cambiarEstado(pedidoId, Pedido.Estado.EN_PREPARACION, usuarioId);

        assertAll(
                () -> assertEquals(Pedido.Estado.EN_PREPARACION, pedido.getEstado()),
                () -> assertNotNull(response)
        );
    }

    @Test
    @DisplayName("cambiarEstado: PENDIENTE → EN_PREPARACION es transición inválida → BadRequestException")
    void cambiarEstado_pendienteAEnPreparacion_lanzaBadRequestException() {
        Pedido pedido = pedidoEnEstado(Pedido.Estado.PENDIENTE);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        assertThrows(BadRequestException.class,
                () -> pedidoService.cambiarEstado(pedidoId, Pedido.Estado.EN_PREPARACION, usuarioId));

        verify(pedidoRepository, never()).save(any());
    }
}
