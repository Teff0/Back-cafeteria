package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Resena;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResenaResponse {

    private UUID id;
    private UUID productoId;
    private String productoNombre;
    private String usuarioNombre;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fecha;

    public static ResenaResponse from(Resena r) {
        return ResenaResponse.builder()
                .id(r.getId())
                .productoId(r.getProducto().getId())
                .productoNombre(r.getProducto().getNombre())
                .usuarioNombre(r.getUsuario().getNombre())
                .calificacion(r.getCalificacion())
                .comentario(r.getComentario())
                .fecha(r.getFecha())
                .build();
    }
}
