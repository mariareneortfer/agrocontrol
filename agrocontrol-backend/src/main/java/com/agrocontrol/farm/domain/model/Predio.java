package com.agrocontrol.farm.domain.model;

/**
 * Modelo de DOMINIO de una propiedad agricola.
 * No lleva @Entity, @Table ni @Column: no sabe nada de JPA ni de PostgreSQL.
 * El mapeo a la tabla agrocontrol.predio esta en PredioJpaEntity.
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
