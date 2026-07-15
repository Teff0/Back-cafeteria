package com.utp.cafeteria.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se detectan demasiados intentos de acceso en poco tiempo
 * (por ejemplo, fuerza bruta contra el login). Devuelve HTTP 429.
 */
@Getter
public class TooManyRequestsException extends RuntimeException {

    private final HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;

    public TooManyRequestsException(String message) {
        super(message);
    }
}
