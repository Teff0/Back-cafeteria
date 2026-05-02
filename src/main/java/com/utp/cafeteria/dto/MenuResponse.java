package com.utp.cafeteria.dto;

import com.utp.cafeteria.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private UUID id;
    private LocalDate fecha;
    private Menu.Horario horario;
    private Boolean activo;
    private List<ProductoResponse> productos;

    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .fecha(menu.getFecha())
                .horario(menu.getHorario())
                .activo(menu.getActivo())
                .productos(menu.getProductos().stream()
                        .map(ProductoResponse::from)
                        .toList())
                .build();
    }
}