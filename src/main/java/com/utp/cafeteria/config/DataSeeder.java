package com.utp.cafeteria.config;

import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea las cuentas iniciales (admin, caja, estudiante) si todavia no existen.
 * Usa el repositorio JDBC y el PasswordEncoder real (BCrypt), por lo que las
 * contraseñas quedan correctamente encriptadas sin incrustar hashes en el SQL.
 *
 * Cuentas por defecto (coinciden con el acceso rapido del frontend):
 *   admin@utp.edu.pe   / admin123    (ADMIN)
 *   caja@utp.edu.pe    / caja123     (CAJA)
 *   cliente@utp.edu.pe / cliente123  (USUARIO)
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crear("ADM001", "admin@utp.edu.pe",   "admin123",   "Administrador", Usuario.Rol.ADMIN);
        crear("CAJ001", "caja@utp.edu.pe",    "caja123",    "Cajero",        Usuario.Rol.CAJA);
        crear("EST001", "cliente@utp.edu.pe", "cliente123", "Estudiante",    Usuario.Rol.USUARIO);
    }

    private void crear(String codigo, String email, String password, String nombre, Usuario.Rol rol) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }
        Usuario u = Usuario.builder()
                .codigo(codigo)
                .email(email)
                .password(passwordEncoder.encode(password))
                .nombre(nombre)
                .rol(rol)
                .activo(true)
                .build();
        usuarioRepository.save(u);
        System.out.println("[INFO] Cuenta creada: " + email + " / " + password + " (" + rol + ")");
    }
}
