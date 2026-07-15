package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.UsuarioRepository;
import com.utp.cafeteria.security.JwtUtil;
import com.utp.cafeteria.security.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();

        // Protección contra fuerza bruta: rechaza si el correo está bloqueado.
        if (loginAttemptService.isBlocked(email)) {
            long minutos = loginAttemptService.minutesRemaining(email);
            throw new TooManyRequestsException(
                "Demasiados intentos fallidos. Intenta nuevamente en " + minutos + " minuto(s).");
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        // Mismo mensaje para correo inexistente y contraseña incorrecta,
        // para no revelar si el correo está registrado.
        if (usuario == null || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            loginAttemptService.loginFailed(email);
            throw new UnauthorizedException("Correo o contraseña inválidos");
        }

        if (!usuario.getActivo()) {
            throw new UnauthorizedException("Usuario inactivo");
        }

        loginAttemptService.loginSucceeded(email); // limpia los intentos previos
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name(), usuario.getId().toString());

        return AuthResponse.of(token, usuario.getEmail(), usuario.getRol().name(), usuario.getNombre());
    }

    public AuthResponse refreshToken(Usuario usuario) {
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name(), usuario.getId().toString());
        return AuthResponse.of(token, usuario.getEmail(), usuario.getRol().name(), usuario.getNombre());
    }
}