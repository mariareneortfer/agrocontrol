package com.agrocontrol.plot.infrastructure.adapter.out.persistence.repository;

import com.agrocontrol.plot.infrastructure.adapter.out.persistence.entity.ParcelaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio Spring Data de parcelas. Solo lo usa ParcelaPersistenceAdapter. */
public interface SpringDataParcelaRepository extends JpaRepository<ParcelaJpaEntity, Long> {

    /** "predio_Id": navega al atributo predio y luego a su id, es decir, la FK predio_id. */
    boolean existsByPredio_IdAndCodigo(Long predioId, String codigo);

    /** Igual que el anterior, pero sin contar a la propia parcela (para PUT). */
    boolean existsByPredio_IdAndCodigoAndIdNot(Long predioId, String codigo, Long id);

    List<ParcelaJpaEntity> findAllByOrderByIdAsc();

    List<ParcelaJpaEntity> findByPredio_IdOrderByIdAsc(Long predioId);
}
