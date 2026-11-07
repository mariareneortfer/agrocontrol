package com.agrocontrol.farm.infrastructure.adapter.in.web.dto;

/**
 * JSON que la API DEVUELVE. A diferencia del request, incluye el id,
 * que no entra (lo genera el sistema) pero si sale.
 */
public record PredioResponse(
        Long id,
        String nombre,
        String ubicacion,
        String descripcion
) {
}
