package com.agrocontrol.plot.domain.port.in;

import com.agrocontrol.plot.domain.model.Parcela;

import java.util.List;
import java.util.Optional;

/** Port IN: consultar parcelas. */
public interface ConsultarParcelaUseCase {

    Optional<Parcela> buscarPorId(Long id);

    /** Todas las parcelas, ordenadas por id (listado general del frontend). */
    List<Parcela> listar();

    /** Todas las parcelas de un predio (la relacion 1:N vista desde el padre). */
    List<Parcela> listarPorPredio(Long predioId);
}
