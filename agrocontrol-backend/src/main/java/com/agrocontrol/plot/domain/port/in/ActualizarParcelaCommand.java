package com.agrocontrol.plot.domain.port.in;

import java.math.BigDecimal;

/**
 * Datos nuevos de una parcela (PUT /api/plots/{id}).
 * predioId puede ser DISTINTO al actual: eso significa mover la parcela a otro predio
 * (reasignar la FK parcela.predio_id).
 */
public record ActualizarParcelaCommand(
        Long predioId,
        String codigo,
        String nombre,
        BigDecimal superficie,
        String unidadSuperficie,
        String ubicacionReferencial
) {
}
