package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Pedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {

    private UUID id;
    private String usuarioEmail;
    private String usuarioNombre;
    private UUID menuId;
    private String menuFecha;
    private Pedido.Estado estado;
    private LocalTime horaProgramada;
    private BigDecimal total;
    private String observaciones;
    private List<ItemPedidoResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPedidoResponse {
        private UUID id;
        private UUID productoId;
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}