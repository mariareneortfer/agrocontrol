package com.agrocontrol.plot.infrastructure.adapter.in.web;

import com.agrocontrol.plot.domain.exception.CodigoParcelaDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Traduce la excepcion de negocio del modulo plot a HTTP 409. */
@RestControllerAdvice
public class ParcelaExceptionHandler {

    @ExceptionHandler(CodigoParcelaDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> manejarDuplicado(CodigoParcelaDuplicadoException ex) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("status", 409);
        respuesta.put("error", "Conflict");
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("timestamp", Instant.now().toString());
        return respuesta;
    }
}
