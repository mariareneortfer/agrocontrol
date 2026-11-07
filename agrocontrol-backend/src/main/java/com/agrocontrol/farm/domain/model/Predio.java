package com.agrocontrol.farm.domain.model;

/**
 * Modelo de DOMINIO de una propiedad agricola.
 * No lleva @Entity, @Table ni @Column: no sabe nada de JPA ni de PostgreSQL.
 *
 * El id es null mientras el predio no se ha guardado: lo genera PostgreSQL (IDENTITY).
 * No se agrega un atributo "activo" porque la tabla predio no tiene esa columna.
 */
public class Predio {

    private final Long id;
    private final String nombre;
    private final String ubicacion;
    private final String descripcion;

    public Predio(Long id, String nombre, String ubicacion, String descripcion) {
        // predio.nombre es NOT NULL.
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del predio es obligatorio");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.ubicacion = ubicacion;     // opcional
        this.descripcion = descripcion; // opcional
    }

    /** Crea un predio nuevo, todavia sin id. */
    public static Predio nuevo(String nombre, String ubicacion, String descripcion) {
        return new Predio(null, nombre, ubicacion, descripcion);
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
