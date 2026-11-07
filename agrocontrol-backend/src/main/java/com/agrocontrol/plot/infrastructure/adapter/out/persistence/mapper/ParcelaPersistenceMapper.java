package com.agrocontrol.plot.infrastructure.adapter.out.persistence.mapper;

import com.agrocontrol.farm.infrastructure.adapter.out.persistence.entity.PredioJpaEntity;
import com.agrocontrol.plot.domain.model.Parcela;
import com.agrocontrol.plot.infrastructure.adapter.out.persistence.entity.ParcelaJpaEntity;

/**
 * La entidad JPA guarda una referencia al predio (objeto);
 * el dominio guarda solo su id (predioId).
 */
public final class ParcelaPersistenceMapper {

    private ParcelaPersistenceMapper() {
    }

    public static ParcelaJpaEntity toJpa(Parcela domain, PredioJpaEntity predioRef) {
        return new ParcelaJpaEntity(
                domain.getId(),
                predioRef,
                domain.getCodigo(),
                domain.getNombre(),
                domain.getSuperficie(),
                domain.getUnidadSuperficie(),
                domain.getUbicacionReferencial());
    }

    public static Parcela toDomain(ParcelaJpaEntity e) {
        return new Parcela(
                e.getId(),
                e.getPredio().getId(), // con LAZY, leer solo el id no hace otra consulta
                e.getCodigo(),
                e.getNombre(),
                e.getSuperficie(),
                e.getUnidadSuperficie(),
                e.getUbicacionReferencial());
    }
}
