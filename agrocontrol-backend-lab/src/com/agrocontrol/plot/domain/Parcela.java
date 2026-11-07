package com.agrocontrol.plot.domain;

import java.math.BigDecimal;

/**
 * Entidad dependiente: representa un lote agricola dentro de un predio
 * (tabla parcela). Cada parcela pertenece a un unico predio.
 */
public class Parcela {

    private final Long id;
    private final String codigo;
    private final String nombre;
    // BigDecimal porque en PostgreSQL es NUMERIC(10,2): valor exacto, no FLOAT.
    private final BigDecimal superficie;
    private final String unidadSuperficie;

    public Parcela(Long id, String codigo, String nombre,
                   BigDecimal superficie, String unidadSuperficie) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo de la parcela es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la parcela es obligatorio");
        }
        // Regla (ck_parcela_superficie_positiva, RN-07): superficie > 0.
        if (superficie == null || superficie.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La superficie debe ser mayor que cero");
        }
        // Regla RN-07: toda cantidad lleva su unidad explicita.
        if (unidadSuperficie == null || unidadSuperficie.isBlank()) {
            throw new IllegalArgumentException("La unidad de superficie es obligatoria");
        }
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.superficie = superficie;
        this.unidadSuperficie = unidadSuperficie;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getSuperficie() {
        return superficie;
    }

    public String getUnidadSuperficie() {
        return unidadSuperficie;
    }

    @Override
    public String toString() {
        return "Parcela{codigo='" + codigo + "', nombre='" + nombre + "', superficie="
                + superficie + " " + unidadSuperficie + "}";
    }
}
