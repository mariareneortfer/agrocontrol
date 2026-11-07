package com.agrocontrol.farm.domain;

import com.agrocontrol.farm.domain.exception.CodigoParcelaDuplicadoException;
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

    // Relacion 1:N vista desde el padre: coleccion privada de parcelas.
    private final List<Parcela> parcelas = new ArrayList<>();

    public Predio(Long id, String nombre, String ubicacion) {
        if (id == null) {
            throw new IllegalArgumentException("El id del predio es obligatorio");
        }
        // predio.nombre es NOT NULL.
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del predio es obligatorio");
        }
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion; // opcional: puede ser null
    }

    /**
     * Relaciona una parcela con este predio.
     * Reglas:
     * - la parcela debe apuntar a este predio (FK parcela.predio_id);
     * - el codigo no se repite dentro del mismo predio (uq_parcela_predio_codigo).
     */
    public void agregarParcela(Parcela parcela) {
        if (parcela == null) {
            throw new IllegalArgumentException("La parcela no puede ser nula");
        }
        if (!id.equals(parcela.getPredioId())) {
            throw new IllegalArgumentException(
                    "La parcela " + parcela.getCodigo() + " no pertenece al predio " + nombre);
        }
        for (Parcela existente : parcelas) {
            if (existente.getCodigo().equalsIgnoreCase(parcela.getCodigo())) {
                throw new CodigoParcelaDuplicadoException(parcela.getCodigo(), nombre);
            }
        }
        parcelas.add(parcela);
    }

    /** Vista de solo lectura: solo se agregan parcelas con agregarParcela(). */
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
