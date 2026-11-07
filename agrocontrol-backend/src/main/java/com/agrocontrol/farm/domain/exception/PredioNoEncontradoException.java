package com.agrocontrol.farm.domain.exception;

/** Se lanza cuando se pide un predio que no existe en la tabla predio. */
public class PredioNoEncontradoException extends RuntimeException {

    public PredioNoEncontradoException(Long predioId) {
        super("No existe el predio con id: " + predioId);
    }
}
