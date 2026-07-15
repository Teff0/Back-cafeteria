package com.utp.cafeteria.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protege el login contra ataques de fuerza bruta.
 * Cuenta los intentos fallidos por correo y bloquea el acceso durante
 * {@link #LOCK_DURATION} tras {@link #MAX_ATTEMPTS} fallos consecutivos.
 *
 * El registro se mantiene en memoria (no requiere base de datos ni ORM):
 * es suficiente para el alcance del proyecto y se limpia al reiniciar.
 */
@Service
public class LoginAttemptService {

    /** Intentos fallidos permitidos antes de bloquear. */
    private static final int MAX_ATTEMPTS = 5;

    /** Tiempo que dura el bloqueo una vez alcanzado el máximo de intentos. */
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private static class Attempt {
        int count;
        Instant lockedUntil;
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /** Indica si el correo está bloqueado en este momento. */
    public boolean isBlocked(String key) {
        Attempt a = attempts.get(normalize(key));
        if (a == null || a.lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(a.lockedUntil)) {
            attempts.remove(normalize(key)); // el bloqueo expiró
            return false;
        }
        return true;
    }

    /** Minutos que faltan para que expire el bloqueo (mínimo 1). */
    public long minutesRemaining(String key) {
        Attempt a = attempts.get(normalize(key));
        if (a == null || a.lockedUntil == null) {
            return 0;
        }
        long seconds = Duration.between(Instant.now(), a.lockedUntil).getSeconds();
        return Math.max(1, (seconds + 59) / 60);
    }

    /** Registra un intento fallido y activa el bloqueo si se alcanza el máximo. */
    public void loginFailed(String key) {
        Attempt a = attempts.computeIfAbsent(normalize(key), x -> new Attempt());
        synchronized (a) {
            a.count++;
            if (a.count >= MAX_ATTEMPTS) {
                a.lockedUntil = Instant.now().plus(LOCK_DURATION);
            }
        }
    }

    /** Limpia el historial tras un inicio de sesión exitoso. */
    public void loginSucceeded(String key) {
        attempts.remove(normalize(key));
    }

    private String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase();
    }
}
