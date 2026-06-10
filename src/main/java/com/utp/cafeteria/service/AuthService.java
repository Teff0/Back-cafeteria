package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.UsuarioRepository;
import com.utp.cafeteria.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Correo o contraseña inválidos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Correo o contraseña inválidos");
        }

        if (!usuario.getActivo()) {
            throw new UnauthorizedException("Usuario inactivo");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name(), usuario.getId().toString());

        return AuthResponse.of(token, usuario.getEmail(), usuario.getRol().name(), usuario.getNombre());
    }

    public AuthResponse refreshToken(Usuario usuario) {
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name(), usuario.getId().toString());
        return AuthResponse.of(token, usuario.getEmail(), usuario.getRol().name(), usuario.getNombre());
    }
}