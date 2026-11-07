package com.agrocontrol.farm.application;

/**
 * Datos de salida del endpoint demo de Predio (entidad padre).
 * Los campos salen de la tabla predio; cantidadParcelas muestra la relacion 1:N con parcela.
 * Spring lo convierte automaticamente a JSON.
 */
public record PredioDemoResponse(
        Long id,
        String nombre,
        String ubicacion,
        int cantidadParcelas
) {
}
