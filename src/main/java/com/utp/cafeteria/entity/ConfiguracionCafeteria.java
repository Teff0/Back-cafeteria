package com.utp.cafeteria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "configuracion_cafeteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionCafeteria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre_cafeteria", nullable = false)
    private String nombreCafeteria;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "mensaje_del_dia", columnDefinition = "TEXT")
    private String mensajeDelDia;

    @Column(name = "hora_apertura")
    private LocalTime horaApertura;

    @Column(name = "hora_cierre")
    private LocalTime horaCierre;

    private String telefono;

    private String direccion;

    @Builder.Default
    @Column(name = "acepta_pedidos", nullable = false)
    private Boolean aceptaPedidos = true;
}
