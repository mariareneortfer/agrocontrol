package com.agrocontrol.farm.infrastructure.adapter.out.persistence.repository;

import com.agrocontrol.farm.infrastructure.adapter.out.persistence.entity.PredioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data: Spring genera la implementacion en tiempo de ejecucion.
 * JpaRepository ya trae save, findById, findAll, etc.
 */
public interface SpringDataPredioRepository extends JpaRepository<PredioJpaEntity, Long> {

    /**
     * Metodo derivado: Spring arma el SQL a partir del nombre del metodo.
     * Lo justifica el filtro opcional GET /api/farms?nombre=...
     */
    List<PredioJpaEntity> findByNombreContainingIgnoreCaseOrderByIdAsc(String nombre);
}
