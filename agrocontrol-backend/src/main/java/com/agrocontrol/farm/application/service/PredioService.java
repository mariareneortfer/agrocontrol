package com.agrocontrol.farm.application.service;

import com.agrocontrol.farm.domain.exception.PredioDuplicadoException;
import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.domain.port.in.ConsultarPredioUseCase;
import com.agrocontrol.farm.domain.port.in.RegistrarPredioUseCase;
import com.agrocontrol.farm.domain.port.out.PredioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso: COMO se registran y consultan predios.
 * Implementa los Port IN y depende SOLO del Port OUT.
 * No importa nada de JPA, Spring Data ni PostgreSQL.
 */
@Service
public class PredioService implements RegistrarPredioUseCase, ConsultarPredioUseCase {

    private final PredioRepositoryPort repositoryPort;

    public PredioService(PredioRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    /**
     * Limite transaccional: la verificacion del nombre y el INSERT ocurren en UNA transaccion.
     * Si algo falla a mitad de camino, PostgreSQL deshace todo (rollback).
     */
    @Override
    @Transactional
    public Predio registrar(Predio predio) {
        // Regla previa: respeta uq_predio_nombre antes de llegar a la base.
        if (repositoryPort.existePorNombre(predio.getNombre())) {
            throw new PredioDuplicadoException(predio.getNombre());
        }
        return repositoryPort.guardar(predio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Predio> buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Predio> listar(String filtroNombre) {
        if (filtroNombre == null || filtroNombre.isBlank()) {
            return repositoryPort.listarTodos();
        }
        return repositoryPort.buscarPorNombreQueContiene(filtroNombre);
    }
}
