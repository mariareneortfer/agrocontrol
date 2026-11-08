package com.agrocontrol.farm.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON que el cliente ENVIA para actualizar un predio (PUT /api/farms/{id}).
 * Hoy tiene los mismos campos que CrearPredioRequest, pero se nombra por su INTENCION:
 * si manana editar exige otros campos, el cambio queda solo aqui.
 * El id NO va en el body: viaja en la URL.
 */
public record ActualizarPredioRequest(
        @NotBlank(message = "El nombre del predio es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 200, message = "La ubicacion no puede superar 200 caracteres")
        String ubicacion,

        @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
        String descripcion
) {
}
