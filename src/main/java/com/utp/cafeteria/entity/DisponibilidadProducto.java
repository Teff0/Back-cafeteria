package com.utp.cafeteria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "disponibilidad_productos",
       uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "fecha"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_restante", nullable = false)
    private Integer cantidadRestante;

    @Column(nullable = false)
    private LocalDate fecha;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
