package com.agrocontrol.shared.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cuando @Valid rechaza un request, Spring responde 400.
 * Este manejador hace que la respuesta 400 muestre que campo fallo
 * y el mensaje escrito en el DTO (por ejemplo, "El nombre del predio es obligatorio").
 */
@RestControllerAdvice
public class ValidacionExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("status", 400);
        respuesta.put("error", "Bad Request");
        respuesta.put("errores", errores);
        respuesta.put("timestamp", Instant.now().toString());
        return respuesta;
    }
}
