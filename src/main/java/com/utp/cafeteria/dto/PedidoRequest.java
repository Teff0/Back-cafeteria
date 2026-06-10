package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Pedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {

    private UUID menuId;

    private LocalTime horaProgramada;

    @NotNull(message = "El método de pago es requerido")
    private Pedido.MetodoPago metodoPago;

    private String observaciones;
    private String codigoPromo;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<ItemPedidoRequest> items;
}
