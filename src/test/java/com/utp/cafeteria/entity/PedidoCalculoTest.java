package com.utp.cafeteria.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pedido — cálculo de totales (lógica pura, sin mocks)")
class PedidoCalculoTest {

    // ── helpers ──────────────────────────────────────────────────────────────

    private ItemPedido item(double precio, int cantidad) {
        ItemPedido i = ItemPedido.builder()
                .precioUnitario(BigDecimal.valueOf(precio))
                .cantidad(cantidad)
                .build();
        i.calcularSubtotal();
        return i;
    }

    private Pedido pedidoConItems(ItemPedido... items) {
        Pedido pedido = Pedido.builder().build();
        for (ItemPedido i : items) pedido.agregarItem(i);
        pedido.calcularTotal();
        return pedido;
    }

    // ── ItemPedido.calcularSubtotal ───────────────────────────────────────────

    @Test
    @DisplayName("calcularSubtotal: precio × cantidad da el subtotal correcto")
    void calcularSubtotal_precioYCantidad_subtotalCorrecto() {
        ItemPedido item = item(5.50, 3);

        assertAll("subtotal",
                () -> assertEquals(0, BigDecimal.valueOf(16.50).compareTo(item.getSubtotal()),
                        "5.50 × 3 debe ser 16.50"),
                () -> assertNotNull(item.getSubtotal(), "subtotal no debe ser null")
        );
    }

    @Test
    @DisplayName("calcularSubtotal: cantidad 1 devuelve el mismo precio unitario")
    void calcularSubtotal_cantidadUno_igualAlPrecio() {
        ItemPedido item = item(12.00, 1);

        assertEquals(0, BigDecimal.valueOf(12.00).compareTo(item.getSubtotal()));
    }

    // ── Pedido.calcularTotal ──────────────────────────────────────────────────

    @Test
    @DisplayName("calcularTotal: pedido sin items devuelve total cero")
    void calcularTotal_pedidoVacio_totalCero() {
        Pedido pedido = Pedido.builder().build();
        pedido.calcularTotal();

        assertAll("pedido vacío",
                () -> assertNotNull(pedido.getTotal(), "total no debe ser null"),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(pedido.getTotal()),
                        "total debe ser 0.00")
        );
    }

    @Test
    @DisplayName("calcularTotal: pedido con un item devuelve el subtotal de ese item")
    void calcularTotal_unItem_totalIgualAlSubtotal() {
        Pedido pedido = pedidoConItems(item(8.00, 2));

        assertAll("un item",
                () -> assertEquals(0, BigDecimal.valueOf(16.00).compareTo(pedido.getTotal()),
                        "8.00 × 2 = 16.00"),
                () -> assertEquals(1, pedido.getItems().size())
        );
    }

    @Test
    @DisplayName("calcularTotal: pedido con múltiples items suma todos los subtotales")
    void calcularTotal_variosItems_sumaTodosLosSubtotales() {
        // 3.00×2=6.00  +  5.50×1=5.50  +  10.00×3=30.00  →  41.50
        Pedido pedido = pedidoConItems(
                item(3.00, 2),
                item(5.50, 1),
                item(10.00, 3)
        );

        assertAll("varios items",
                () -> assertEquals(0, BigDecimal.valueOf(41.50).compareTo(pedido.getTotal()),
                        "total esperado: 41.50"),
                () -> assertEquals(3, pedido.getItems().size())
        );
    }

    @Test
    @DisplayName("calcularTotal: llamar dos veces produce el mismo resultado (idempotente)")
    void calcularTotal_llamadaDosVeces_mismoResultado() {
        Pedido pedido = pedidoConItems(item(7.00, 2));
        pedido.calcularTotal(); // segunda llamada

        assertEquals(0, BigDecimal.valueOf(14.00).compareTo(pedido.getTotal()));
    }
}
