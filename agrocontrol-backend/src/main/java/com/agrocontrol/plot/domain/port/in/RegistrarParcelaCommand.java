package com.agrocontrol.plot.domain.port.in;

import java.math.BigDecimal;

/**
 * Datos que necesita el caso de uso "registrar parcela".
 * El controller lo arma a partir del Request DTO; el caso de uso construye el dominio.
 */
public record RegistrarParcelaCommand(
        Long predioId,
        String codigo,
        String nombre,
        BigDecimal superficie,
        String unidadSuperficie,
        String ubicacionReferencial
) {
}
