package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.ConfiguracionCafeteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionCafeteriaResponse {

    private UUID id;
    private String nombreCafeteria;
    private String logoUrl;
    private String mensajeDelDia;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private String telefono;
    private String direccion;
    private Boolean aceptaPedidos;

    public static ConfiguracionCafeteriaResponse from(ConfiguracionCafeteria c) {
        return ConfiguracionCafeteriaResponse.builder()
                .id(c.getId())
                .nombreCafeteria(c.getNombreCafeteria())
                .logoUrl(c.getLogoUrl())
                .mensajeDelDia(c.getMensajeDelDia())
                .horaApertura(c.getHoraApertura())
                .horaCierre(c.getHoraCierre())
                .telefono(c.getTelefono())
                .direccion(c.getDireccion())
                .aceptaPedidos(c.getAceptaPedidos())
                .build();
    }
}
