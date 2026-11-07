package com.agrocontrol.plot.application.service;

import com.agrocontrol.farm.domain.exception.PredioNoEncontradoException;
import com.agrocontrol.farm.domain.port.in.ConsultarPredioUseCase;
import com.agrocontrol.plot.domain.exception.CodigoParcelaDuplicadoException;
import com.agrocontrol.plot.domain.model.Parcela;
import com.agrocontrol.plot.domain.port.in.ConsultarParcelaUseCase;
import com.agrocontrol.plot.domain.port.in.RegistrarParcelaUseCase;
import com.agrocontrol.plot.domain.port.out.ParcelaRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso de parcelas.
 * Para saber si el predio existe usa el Port IN publico del modulo farm
 * (ConsultarPredioUseCase), NUNCA el repositorio JPA de predios.
 */
@Service
public class ParcelaService implements RegistrarParcelaUseCase, ConsultarParcelaUseCase {

    private final ParcelaRepositoryPort repositoryPort;
    private final ConsultarPredioUseCase consultarPredioUseCase;

    public ParcelaService(ParcelaRepositoryPort repositoryPort,
                          ConsultarPredioUseCase consultarPredioUseCase) {
        this.repositoryPort = repositoryPort;
        this.consultarPredioUseCase = consultarPredioUseCase;
    }

    @Override
    public Parcela registrar(Parcela parcela) {
        // 1. El predio padre debe existir (FK parcela.predio_id).
        verificarPredio(parcela.getPredioId());
        // 2. El codigo no se repite dentro del mismo predio (uq_parcela_predio_codigo).
        if (repositoryPort.existePorPredioIdYCodigo(parcela.getPredioId(), parcela.getCodigo())) {
            throw new CodigoParcelaDuplicadoException(parcela.getCodigo(), parcela.getPredioId());
        }
        return repositoryPort.guardar(parcela);
    }

    @Override
    public Optional<Parcela> buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id);
    }

    @Override
    public List<Parcela> listarPorPredio(Long predioId) {
        verificarPredio(predioId);
        return repositoryPort.listarPorPredioId(predioId);
    }

    private void verificarPredio(Long predioId) {
        if (consultarPredioUseCase.buscarPorId(predioId).isEmpty()) {
            throw new PredioNoEncontradoException(predioId);
        }
    }
}
