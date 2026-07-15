package com.utp.cafeteria.entity;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoItem {

    private UUID id;
    private Usuario usuario;
    private Producto producto;
    private Integer cantidad;

    public void calcularSubtotal() {
    }
}
