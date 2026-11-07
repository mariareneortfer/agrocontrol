package com.agrocontrol.plot.domain.model;

import java.math.BigDecimal;

/**
 * Modelo de DOMINIO de una parcela (lote agricola dentro de un predio).
 * Sin @Entity, sin @ManyToOne: la relacion con el predio se guarda solo como predioId,
 * igual que la columna FK parcela.predio_id.
 *
 * No hay enum: la tabla parcela no tiene un CHECK con valores cerrados.
 * Su CHECK real es ck_parcela_superficie_positiva (superficie > 0), que se valida aqui.
 */
public class Parcela {

    private final Long id;
    private final Long predioId;
    private final String codigo;
    private final String nombre;
    private final BigDecimal superficie;
    private final String unidadSuperficie;
    private final String ubicacionReferencial;

    public Parcela(Long id, Long predioId, String codigo, String nombre,
                   BigDecimal superficie, String unidadSuperficie, String ubicacionReferencial) {
        // parcela.predio_id NOT NULL: toda parcela pertenece a un predio.
        if (predioId == null) {
            throw new IllegalArgumentException("La parcela debe pertenecer a un predio");
        }
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo de la parcela es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la parcela es obligatorio");
        }
        // ck_parcela_superficie_positiva (RN-07).
        if (superficie == null || superficie.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La superficie debe ser mayor que cero");
        }
        if (unidadSuperficie == null || unidadSuperficie.isBlank()) {
            throw new IllegalArgumentException("La unidad de superficie es obligatoria");
        }
        this.id = id;
        this.predioId = predioId;
        // Normalizacion: "p-01" y "P-01" son el mismo codigo.
        this.codigo = codigo.trim().toUpperCase();
        this.nombre = nombre.trim();
        this.superficie = superficie;
        this.unidadSuperficie = unidadSuperficie.trim();
        this.ubicacionReferencial = ubicacionReferencial; // opcional
    }

    /** Crea una parcela nueva, todavia sin id (lo genera PostgreSQL). */
    public static Parcela nueva(Long predioId, String codigo, String nombre, BigDecimal superficie,
                                String unidadSuperficie, String ubicacionReferencial) {
        return new Parcela(null, predioId, codigo, nombre, superficie, unidadSuperficie, ubicacionReferencial);
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

    public String getUbicacionReferencial() {
        return ubicacionReferencial;
    }
}
