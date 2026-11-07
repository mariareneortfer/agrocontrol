package com.agrocontrol.farm.domain;

import com.agrocontrol.plot.domain.Parcela;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad padre: representa una propiedad agricola (tabla predio).
 * Un predio puede tener muchas parcelas (relacion 1:N).
 */
public class Predio {

    private final Long id;
    private final String nombre;
    private final String ubicacion;

    // Relacion 1:N. En PostgreSQL es la FK parcela.predio_id;
    // en Java es una coleccion privada de objetos Parcela.
    private final List<Parcela> parcelas = new ArrayList<>();

    public Predio(Long id, String nombre, String ubicacion) {
        // Regla: el nombre es obligatorio (predio.nombre NOT NULL).
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del predio es obligatorio");
        }
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion; // opcional: puede ser null
    }

    /**
     * Metodo de negocio para relacionar una parcela con este predio.
     * Regla (uq_parcela_predio_codigo, RF-01): el codigo de una parcela
     * no se puede repetir dentro del mismo predio.
     */
    public void agregarParcela(Parcela parcela) {
        if (parcela == null) {
            throw new IllegalArgumentException("La parcela no puede ser nula");
        }
        for (Parcela existente : parcelas) {
            if (existente.getCodigo().equalsIgnoreCase(parcela.getCodigo())) {
                throw new IllegalStateException(
                        "Ya existe la parcela " + parcela.getCodigo() + " en el predio " + nombre);
            }
        }
        parcelas.add(parcela);
    }

    /**
     * Devuelve una vista de solo lectura: nadie puede agregar o quitar
     * parcelas sin pasar por agregarParcela().
     */
    public List<Parcela> getParcelas() {
        return Collections.unmodifiableList(parcelas);
    }

    public int cantidadParcelas() {
        return parcelas.size();
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

    @Override
    public String toString() {
        return "Predio{id=" + id + ", nombre='" + nombre + "', parcelas=" + parcelas.size() + "}";
    }
}
