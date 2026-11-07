package com.agrocontrol.farm.domain;

/**
 * Entidad padre del dominio: una propiedad agricola (tabla predio).
 * Es una clase de dominio, NO el contrato de la API: hacia afuera
 * se exponen CrearPredioRequest y PredioResponse.
 */
public class Predio {

    private final Long id;
    private final String nombre;
    private final String ubicacion;
    private final String descripcion;

    public Predio(Long id, String nombre, String ubicacion, String descripcion) {
        if (id == null) {
            throw new IllegalArgumentException("El id del predio es obligatorio");
        }
        // predio.nombre es NOT NULL.
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del predio es obligatorio");
        }
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;     // opcional
        this.descripcion = descripcion; // opcional
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
