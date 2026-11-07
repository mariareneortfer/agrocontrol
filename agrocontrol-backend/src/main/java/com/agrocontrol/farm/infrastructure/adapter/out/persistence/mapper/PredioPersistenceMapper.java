package com.agrocontrol.farm.infrastructure.adapter.out.persistence.mapper;

import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.infrastructure.adapter.out.persistence.entity.PredioJpaEntity;

/**
 * Traduce entre la entidad de persistencia (JPA) y el modelo de dominio.
 * Asi el dominio no depende de JPA.
 */
public final class PredioPersistenceMapper {

    private PredioPersistenceMapper() {
    }

    public static Predio toDomain(PredioJpaEntity entity) {
        return new Predio(
                entity.getId(),
                entity.getNombre(),
                entity.getUbicacion(),
                entity.getDescripcion());
    }
}
