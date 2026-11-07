package com.agrocontrol.farm.application;

import com.agrocontrol.farm.domain.Predio;
import com.agrocontrol.farm.domain.exception.NombrePredioDuplicadoException;
import com.agrocontrol.farm.domain.exception.PredioNoEncontradoException;
import com.agrocontrol.farm.domain.port.PredioRepository;
import com.agrocontrol.plot.application.command.RegistrarParcelaCommand;
import com.agrocontrol.plot.domain.Parcela;

import java.util.List;

/**
 * Servicio Java puro: coordina los casos de uso de predios y parcelas.
 * Depende de la INTERFAZ PredioRepository, nunca de una implementacion concreta.
 */
public class PredioService {

    private final PredioRepository repository;

    // Temporal: genera ids de parcela mientras no exista PostgreSQL
    // (en la base los genera la columna IDENTITY).
    private long siguienteParcelaId = 1L;

    public PredioService(PredioRepository repository) {
        this.repository = repository;
    }

    public Predio registrar(Predio predio) {
        if (repository.existePorNombre(predio.getNombre())) {
            throw new NombrePredioDuplicadoException(predio.getNombre());
        }
        return repository.guardar(predio);
    }

    public Predio obtener(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new PredioNoEncontradoException(id));
    }

    public List<Predio> listar() {
        return repository.listarTodos();
    }

    /** Caso de uso: registrar una parcela dentro de un predio existente. */
    public Parcela registrarParcela(RegistrarParcelaCommand command) {
        Predio predio = obtener(command.predioId()); // lanza PredioNoEncontradoException
        Parcela parcela = new Parcela(
                siguienteParcelaId,
                predio.getId(),
                command.codigo(),
                command.nombre(),
                command.superficie(),
                command.unidadSuperficie());
        predio.agregarParcela(parcela); // lanza CodigoParcelaDuplicadoException
        siguienteParcelaId++;
        repository.guardar(predio);
        return parcela;
    }
}
