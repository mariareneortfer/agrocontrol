package com.agrocontrol.plot.domain.port.out;

import com.agrocontrol.plot.domain.model.Parcela;

import java.util.List;
import java.util.Optional;

/** Port OUT: lo que el nucleo de parcelas necesita para guardar y leer. Sin JPA. */
public interface ParcelaRepositoryPort {

    Parcela guardar(Parcela parcela);

    Optional<Parcela> buscarPorId(Long id);

    List<Parcela> listarTodas();

    List<Parcela> listarPorPredioId(Long predioId);

    /** Apoya la regla UNIQUE (predio_id, codigo): uq_parcela_predio_codigo. */
    boolean existePorPredioIdYCodigo(Long predioId, String codigo);
}
