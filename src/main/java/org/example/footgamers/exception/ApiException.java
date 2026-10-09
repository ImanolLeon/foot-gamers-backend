package org.example.footgamers.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(mensaje, HttpStatus.NOT_FOUND);
    }

    public static ApiException valorYaEnUso(String mensaje) {
        return new ApiException(mensaje, HttpStatus.CONFLICT);
    }

    public static ApiException solicitudInvalida(String mensaje) {
        return new ApiException(mensaje, HttpStatus.BAD_REQUEST);
    }

    public static ApiException noAutorizado(String mensaje) {
        return new ApiException(mensaje, HttpStatus.UNAUTHORIZED);
    }
}