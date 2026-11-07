package com.agrocontrol.farm.domain.exception;

/** Regla UNIQUE (predio_id, codigo) de la tabla parcela (uq_parcela_predio_codigo, RF-01). */
public class CodigoParcelaDuplicadoException extends RuntimeException {

    public CodigoParcelaDuplicadoException(String codigo, String nombrePredio) {
        super("Ya existe la parcela " + codigo + " en el predio " + nombrePredio);
    }
}
