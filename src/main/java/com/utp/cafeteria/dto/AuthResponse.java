package com.utp.cafeteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String email;
    private String rol;
    private String nombre;

    public static AuthResponse of(String token, String email, String rol, String nombre) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(email)
                .rol(rol)
                .nombre(nombre)
                .build();
    }
}