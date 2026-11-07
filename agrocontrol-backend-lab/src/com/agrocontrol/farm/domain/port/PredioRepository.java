package com.agrocontrol.farm.domain.port;

import com.agrocontrol.farm.domain.Predio;

import java.util.List;
import java.util.Optional;

/**
 * Contrato (puerto): declara QUE operaciones necesita la aplicacion
 * sobre los predios, sin decir COMO ni DONDE se guardan.
 * No menciona PostgreSQL, SQL ni JPA.
 */
public interface PredioRepository {

    Predio guardar(Predio predio);

    /** Puede no encontrar nada: por eso devuelve Optional y no null. */
    Optional<Predio> buscarPorId(Long id);

    List<Predio> listarTodos();

    /** Apoya la regla UNIQUE de predio.nombre (uq_predio_nombre). */
    boolean existePorNombre(String nombre);
}
