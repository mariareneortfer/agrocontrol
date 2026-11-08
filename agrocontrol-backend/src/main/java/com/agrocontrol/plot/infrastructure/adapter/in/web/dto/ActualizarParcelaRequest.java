package com.agrocontrol.plot.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * JSON para actualizar una parcela (PUT /api/plots/{id}). El id viaja en la URL.
 * Si predioId cambia, la parcela se mueve a ese predio.
 */
public record ActualizarParcelaRequest(
        @NotNull(message = "El predioId es obligatorio")
        Long predioId,

        @NotBlank(message = "El codigo de la parcela es obligatorio")
        @Size(max = 20, message = "El codigo no puede superar 20 caracteres")
        String codigo,

        @NotBlank(message = "El nombre de la parcela es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotNull(message = "La superficie es obligatoria")
        @Positive(message = "La superficie debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "La superficie admite hasta 8 enteros y 2 decimales")
        BigDecimal superficie,

        @NotBlank(message = "La unidad de superficie es obligatoria")
        @Size(max = 20, message = "La unidad no puede superar 20 caracteres")
        String unidadSuperficie,

        @Size(max = 200, message = "La ubicacion referencial no puede superar 200 caracteres")
        String ubicacionReferencial
) {
}
