package com.agrocontrol.plot.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/** JSON que devuelve la API para una parcela. Incluye el id y el predioId (la FK). */
public record ParcelaResponse(
        Long id,
        Long predioId,
        String codigo,
        String nombre,
        BigDecimal superficie,
        String unidadSuperficie,
        String ubicacionReferencial
) {
}
