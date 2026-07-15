package com.utp.cafeteria.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    private UUID id;
    private Usuario usuario;
    private Menu menu;
    private Estado estado;
    private LocalTime horaProgramada;

    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    private String observaciones;

    private String voucherUrl;

    @Builder.Default
    private List<ItemPedido> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Estado {
        PENDIENTE,
        PAGADO,
        EN_PREPARACION,
        LISTO,
        ENTREGADO,
        CANCELADO
    }

    public void agregarItem(ItemPedido item) {
        items.add(item);
        item.setPedido(this);
    }

    public void calcularTotal() {
        this.total = items.stream()
            .map(ItemPedido::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
