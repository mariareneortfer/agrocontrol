package com.agrocontrol.farm.infrastructure.adapter.out.persistence.mapper;

import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.infrastructure.adapter.out.persistence.entity.PredioJpaEntity;

/** Traduce entre el modelo de dominio y la entidad JPA. */
public final class PredioPersistenceMapper {

    private PredioPersistenceMapper() {
    }

    public static PredioJpaEntity toEntity(Predio domain) {
        return new PredioJpaEntity(
                domain.getId(),
                domain.getNombre(),
                domain.getUbicacion(),
                domain.getDescripcion());
    }

    public static Predio toDomain(PredioJpaEntity entity) {
        return new Predio(
                entity.getId(),
                entity.getNombre(),
                entity.getUbicacion(),
                entity.getDescripcion());
    }
}
