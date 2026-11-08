package com.agrocontrol.plot.domain.exception;

/**
 * Regla de integridad: una parcela que ya participa en alguna campana
 * (tabla puente campana_parcela) no se puede eliminar (409).
 * La protege la FK de campana_parcela hacia parcela; el adaptador traduce la violacion.
 */
public class ParcelaEnUsoException extends RuntimeException {

    public ParcelaEnUsoException(String codigo) {
        super("No se puede eliminar la parcela " + codigo
                + " porque esta asociada a una o mas campanas.");
    }
}
