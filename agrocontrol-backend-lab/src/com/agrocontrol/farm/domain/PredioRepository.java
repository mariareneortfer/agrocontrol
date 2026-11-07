package com.agrocontrol.farm.domain;

import java.util.Optional;

/**
 * Contrato: declara QUE necesitamos hacer con los predios,
 * no COMO se hara. Todavia no hay PostgreSQL ni JPA detras.
 */
public interface PredioRepository {

    Predio guardar(Predio predio);

    Optional<Predio> buscarPorId(Long id);
}
