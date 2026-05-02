package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SupabaseAuthService supabaseAuthService;

    public AuthResponse registro(RegistroRequest request) {
        if (!request.getEmail().endsWith("@utp.edu.pe") && !request.getEmail().endsWith("@utp.pe")) {
            throw new BadRequestException("Debe usar un correo institucional (@utp.edu.pe o @utp.pe)");
        }

        String rol = request.getRol() != null ? request.getRol().toUpperCase() : "ESTUDIANTE";
        if (!rol.equals("ESTUDIANTE") && !rol.equals("ADMINISTRATIVO")) {
            throw new BadRequestException("Rol inválido. Use ESTUDIANTE o ADMINISTRATIVO");
        }

        return supabaseAuthService.signUp(
            request.getEmail(), 
            request.getPassword(), 
            request.getNombre(), 
            rol
        );
    }

    public AuthResponse login(LoginRequest request) {
        return supabaseAuthService.signIn(request.getEmail(), request.getPassword());
    }
}