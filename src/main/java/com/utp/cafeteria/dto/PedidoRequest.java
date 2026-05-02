package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Menu;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {

    private UUID menuId;

    @NotBlank(message = "La hora programada es requerida")
    private LocalTime horaProgramada;

    private String observaciones;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<ItemPedidoRequest> items;
}