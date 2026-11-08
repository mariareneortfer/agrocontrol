package com.agrocontrol.farm.domain.exception;

/**
 * Regla de integridad 1:N: un predio que todavia tiene parcelas no se puede eliminar.
 * La protege PostgreSQL con fk_parcela_predio (sin ON DELETE CASCADE);
 * el adaptador de persistencia traduce esa violacion a esta excepcion de dominio (409).
 */
public class PredioConParcelasException extends RuntimeException {

    public PredioConParcelasException(String nombre) {
        super("No se puede eliminar el predio " + nombre
                + " porque tiene parcelas asociadas. Elimine o mueva primero sus parcelas.");
    }
}
