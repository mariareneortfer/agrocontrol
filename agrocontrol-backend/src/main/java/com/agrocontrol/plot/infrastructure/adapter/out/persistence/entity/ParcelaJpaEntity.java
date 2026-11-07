package com.agrocontrol.plot.infrastructure.adapter.out.persistence.entity;

import com.agrocontrol.farm.infrastructure.adapter.out.persistence.entity.PredioJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Entidad de persistencia de la tabla dependiente agrocontrol.parcela.
 * La relacion 1:N se mapea en este lado (el lado N), porque aqui esta la FK parcela.predio_id.
 * Predio NO tiene una coleccion @OneToMany: todavia no se necesita navegar
 * desde el predio hacia todas sus parcelas.
 */
@Entity
@Table(name = "parcela")
public class ParcelaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parcela_id")
    private Long id;

    // FK parcela.predio_id -> predio.predio_id (fk_parcela_predio), NOT NULL.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "predio_id", nullable = false)
    private PredioJpaEntity predio;

    // codigo VARCHAR(20) NOT NULL; UNIQUE junto con predio_id (uq_parcela_predio_codigo).
    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    // superficie NUMERIC(10,2) NOT NULL: BigDecimal para conservar el valor exacto.
    @Column(name = "superficie", nullable = false, precision = 10, scale = 2)
    private BigDecimal superficie;

    @Column(name = "unidad_superficie", nullable = false, length = 20)
    private String unidadSuperficie;

    @Column(name = "ubicacion_referencial", length = 200)
    private String ubicacionReferencial;

    protected ParcelaJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public PredioJpaEntity getPredio() {
        return predio;
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
