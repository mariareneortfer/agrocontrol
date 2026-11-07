package com.agrocontrol.farm.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON que el cliente ENVIA para crear un predio (POST /api/farms).
 * No lleva id: lo genera el sistema.
 * Los limites salen del modelo fisico: nombre VARCHAR(100), ubicacion VARCHAR(200).
 */
public record CrearPredioRequest(

        @NotBlank(message = "El nombre del predio es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 200, message = "La ubicacion no puede superar 200 caracteres")
        String ubicacion,

        @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
        String descripcion
) {
}
