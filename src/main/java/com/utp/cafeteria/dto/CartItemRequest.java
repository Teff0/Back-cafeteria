package com.utp.cafeteria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CartItemRequest {
    @NotNull
    private UUID productoId;
    
    @Min(1)
    private Integer cantidad = 1;
}