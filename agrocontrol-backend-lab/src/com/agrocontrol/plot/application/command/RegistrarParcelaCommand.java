package com.agrocontrol.plot.application.command;

import java.math.BigDecimal;

/**
 * Datos que necesita la operacion "registrar una parcela", y nada mas.
 * No lleva id (lo genera el sistema) ni ubicacion_referencial (es opcional).
 * Un record es inmutable: Java genera el constructor, los getters, equals y toString.
 */
public record RegistrarParcelaCommand(
        Long predioId,
        String codigo,
        String nombre,
        BigDecimal superficie,
        String unidadSuperficie
) {
}
