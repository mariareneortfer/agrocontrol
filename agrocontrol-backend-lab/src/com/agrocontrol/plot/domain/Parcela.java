package com.agrocontrol.plot.domain;

import java.math.BigDecimal;

/**
 * Entidad dependiente: representa un lote agricola dentro de un predio
 * (tabla parcela). Cada parcela pertenece a un unico predio.
 */
public class Parcela {

    private final Long id;
    // Relacion 1:N vista desde el hijo: id del padre, igual que la FK parcela.predio_id.
    private final Long predioId;
    private final String codigo;
    private final String nombre;
    // BigDecimal porque en PostgreSQL es NUMERIC(10,2): valor exacto, no FLOAT.
    private final BigDecimal superficie;
    private final String unidadSuperficie;

    public Parcela(Long id, Long predioId, String codigo, String nombre,
                   BigDecimal superficie, String unidadSuperficie) {
        // FK obligatoria: parcela.predio_id es NOT NULL.
        if (predioId == null) {
            throw new IllegalArgumentException("La parcela debe pertenecer a un predio");
        }
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo de la parcela es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la parcela es obligatorio");
        }
        // ck_parcela_superficie_positiva (RN-07): superficie > 0.
        if (superficie == null || superficie.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La superficie debe ser mayor que cero");
        }
        // RN-07: toda cantidad lleva su unidad explicita.
        if (unidadSuperficie == null || unidadSuperficie.isBlank()) {
            throw new IllegalArgumentException("La unidad de superficie es obligatoria");
        }
        this.id = id;
        this.predioId = predioId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.superficie = superficie;
        this.unidadSuperficie = unidadSuperficie;
    }

    public Long getId() {
        return id;
    }

    public Long getPredioId() {
        return predioId;
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
        return "Parcela{id=" + id + ", predioId=" + predioId + ", codigo='" + codigo
                + "', nombre='" + nombre + "', superficie=" + superficie + " " + unidadSuperficie + "}";
    }
}
