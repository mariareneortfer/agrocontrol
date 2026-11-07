package com.agrocontrol.plot.domain.exception;

/**
 * Regla de negocio: el codigo de una parcela no se repite dentro del mismo predio.
 * Corresponde a uq_parcela_predio_codigo (predio_id, codigo).
 */
public class CodigoParcelaDuplicadoException extends RuntimeException {

    public CodigoParcelaDuplicadoException(String codigo, Long predioId) {
        super("Ya existe la parcela " + codigo + " en el predio " + predioId);
    }
}
