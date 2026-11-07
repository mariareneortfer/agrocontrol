package com.agrocontrol.farm.domain.exception;

/** Se lanza cuando se busca un predio con un id que no existe. */
public class PredioNoEncontradoException extends RuntimeException {

    public PredioNoEncontradoException(Long id) {
        super("No existe el predio con id: " + id);
    }
}
