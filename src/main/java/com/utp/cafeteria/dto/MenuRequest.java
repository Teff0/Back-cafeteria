package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Menu;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {

    @NotNull(message = "La fecha es requerida")
    private LocalDate fecha;

    @NotNull(message = "El horario es requerido")
    private Menu.Horario horario;

    private Set<UUID> productoIds;
}
