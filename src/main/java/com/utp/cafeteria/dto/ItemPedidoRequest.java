package com.utp.cafeteria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoRequest {

    @NotNull(message = "El ID del producto es requerido")
    private UUID productoId;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad minima es 1")
    private Integer cantidad;

    private String nota;
}
