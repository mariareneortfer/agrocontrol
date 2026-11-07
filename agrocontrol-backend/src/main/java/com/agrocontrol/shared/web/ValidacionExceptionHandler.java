package com.agrocontrol.shared.web;

import org.springframework.dao.DataIntegrityViolationException;
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
 * Traduce errores a respuestas HTTP claras:
 * - 400 cuando @Valid rechaza el request (antes de llegar a la base).
 * - 409 cuando PostgreSQL rechaza la operacion por una restriccion (por ejemplo, UNIQUE).
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> manejarIntegridad(DataIntegrityViolationException ex) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("status", 409);
        respuesta.put("error", "Conflict");
        respuesta.put("mensaje", "PostgreSQL rechazo la operacion: el dato viola una restriccion de la base "
                + "(por ejemplo, un nombre de predio que ya existe).");
        respuesta.put("timestamp", Instant.now().toString());
        return respuesta;
    }
}
