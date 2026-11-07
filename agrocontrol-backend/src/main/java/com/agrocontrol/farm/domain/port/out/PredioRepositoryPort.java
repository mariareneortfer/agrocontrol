package com.agrocontrol.farm.domain.port.out;

import com.agrocontrol.farm.domain.model.Predio;

import java.util.List;
import java.util.Optional;

/**
 * Port OUT: lo que el nucleo NECESITA del exterior para guardar y leer predios.
 * Esta en el dominio y no menciona JPA, SQL ni PostgreSQL.
 * Lo implementa PredioPersistenceAdapter (infraestructura).
 */
public interface PredioRepositoryPort {

    Predio guardar(Predio predio);

    Optional<Predio> buscarPorId(Long id);

    List<Predio> listarTodos();

    List<Predio> buscarPorNombreQueContiene(String texto);

    /** Apoya la regla UNIQUE de predio.nombre (uq_predio_nombre). */
    boolean existePorNombre(String nombre);
}
