package com.agrocontrol.plot.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * JSON para crear una parcela (POST /api/plots). No lleva id: lo genera PostgreSQL.
 * Los limites salen del modelo fisico de la tabla parcela.
 */
public record CrearParcelaRequest(

        // parcela.predio_id NOT NULL (FK hacia predio).
        @NotNull(message = "El predioId es obligatorio")
        Long predioId,

        // codigo VARCHAR(20) NOT NULL.
        @NotBlank(message = "El codigo de la parcela es obligatorio")
        @Size(max = 20, message = "El codigo no puede superar 20 caracteres")
        String codigo,

        // nombre VARCHAR(100) NOT NULL.
        @NotBlank(message = "El nombre de la parcela es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        // superficie NUMERIC(10,2) NOT NULL con CHECK superficie > 0.
        @NotNull(message = "La superficie es obligatoria")
        @Positive(message = "La superficie debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "La superficie admite hasta 8 enteros y 2 decimales")
        BigDecimal superficie,

        // unidad_superficie VARCHAR(20) NOT NULL (RN-07: unidad explicita).
        @NotBlank(message = "La unidad de superficie es obligatoria")
        @Size(max = 20, message = "La unidad no puede superar 20 caracteres")
        String unidadSuperficie,

        // ubicacion_referencial VARCHAR(200), opcional.
        @Size(max = 200, message = "La ubicacion referencial no puede superar 200 caracteres")
        String ubicacionReferencial
) {
}
