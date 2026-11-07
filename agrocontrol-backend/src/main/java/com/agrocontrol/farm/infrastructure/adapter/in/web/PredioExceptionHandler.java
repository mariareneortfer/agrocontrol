package com.agrocontrol.farm.infrastructure.adapter.in.web;

import com.agrocontrol.farm.domain.exception.PredioDuplicadoException;
import com.agrocontrol.farm.domain.exception.PredioNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones de negocio del modulo farm a respuestas HTTP:
 * 409 si el nombre del predio ya existe; 404 si el predio no existe.
 * Esta en el adaptador web: el dominio no sabe nada de HTTP.
 */
@RestControllerAdvice
public class PredioExceptionHandler {

    @ExceptionHandler(PredioDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> manejarDuplicado(PredioDuplicadoException ex) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("status", 409);
        respuesta.put("error", "Conflict");
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("timestamp", Instant.now().toString());
        return respuesta;
    }

    @ExceptionHandler(PredioNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> manejarNoEncontrado(PredioNoEncontradoException ex) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("status", 404);
        respuesta.put("error", "Not Found");
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("timestamp", Instant.now().toString());
        return respuesta;
    }
}
