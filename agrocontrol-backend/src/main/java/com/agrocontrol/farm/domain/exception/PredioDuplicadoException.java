package com.agrocontrol.farm.domain.exception;

/**
 * Regla de negocio: no puede haber dos predios con el mismo nombre.
 * Corresponde a la restriccion UNIQUE uq_predio_nombre de la tabla predio.
 */
public class PredioDuplicadoException extends RuntimeException {

    public PredioDuplicadoException(String nombre) {
        super("Ya existe un predio con el nombre: " + nombre);
    }
}
