package com.agrocontrol.farm.infrastructure.adapter.in.web;

import com.agrocontrol.farm.domain.exception.PredioDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce la excepcion de negocio del modulo farm a una respuesta HTTP 409.
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
}
