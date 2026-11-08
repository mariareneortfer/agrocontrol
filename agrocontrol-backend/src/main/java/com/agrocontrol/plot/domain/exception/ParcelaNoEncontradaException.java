package com.agrocontrol.plot.domain.exception;

/** Se lanza cuando se pide una parcela que no existe en la tabla parcela (404). */
public class ParcelaNoEncontradaException extends RuntimeException {

    public ParcelaNoEncontradaException(Long parcelaId) {
        super("No existe la parcela con id: " + parcelaId);
    }
}
