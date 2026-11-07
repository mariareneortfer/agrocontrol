package com.agrocontrol.farm.domain.exception;

/** Regla UNIQUE de predio.nombre (uq_predio_nombre, RF-01). */
public class NombrePredioDuplicadoException extends RuntimeException {

    public NombrePredioDuplicadoException(String nombre) {
        super("Ya existe un predio con el nombre: " + nombre);
    }
}
