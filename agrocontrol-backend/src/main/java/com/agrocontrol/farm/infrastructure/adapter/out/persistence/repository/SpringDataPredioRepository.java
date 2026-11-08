package com.agrocontrol.farm.infrastructure.adapter.out.persistence.repository;

import com.agrocontrol.farm.infrastructure.adapter.out.persistence.entity.PredioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data: Spring genera la implementacion.
 * SOLO lo conoce PredioPersistenceAdapter; ni el controller ni el caso de uso lo ven.
 */
public interface SpringDataPredioRepository extends JpaRepository<PredioJpaEntity, Long> {

    /** Para la regla UNIQUE de predio.nombre. */
    boolean existsByNombre(String nombre);

    /** Para actualizar: hay OTRO predio (id distinto) con ese nombre. */
    boolean existsByNombreAndIdNot(String nombre, Long id);

    /** Para el filtro opcional GET /api/farms?nombre=... */
    List<PredioJpaEntity> findByNombreContainingIgnoreCaseOrderByIdAsc(String nombre);
}
