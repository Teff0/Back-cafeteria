package com.utp.cafeteria.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionCafeteriaRequest {

    @NotBlank(message = "El nombre de la cafeteria es requerido")
    private String nombreCafeteria;

    private String logoUrl;
    private String mensajeDelDia;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private String telefono;
    private String direccion;
    private Boolean aceptaPedidos;
}
