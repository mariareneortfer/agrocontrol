package com.agrocontrol.plot.application.service;

import com.agrocontrol.farm.domain.exception.PredioNoEncontradoException;
import com.agrocontrol.farm.domain.port.in.ConsultarPredioUseCase;
import com.agrocontrol.plot.domain.exception.CodigoParcelaDuplicadoException;
import com.agrocontrol.plot.domain.exception.ParcelaNoEncontradaException;
import com.agrocontrol.plot.domain.model.Parcela;
import com.agrocontrol.plot.domain.port.in.ActualizarParcelaCommand;
import com.agrocontrol.plot.domain.port.in.ActualizarParcelaUseCase;
import com.agrocontrol.plot.domain.port.in.ConsultarParcelaUseCase;
import com.agrocontrol.plot.domain.port.in.EliminarParcelaUseCase;
import com.agrocontrol.plot.domain.port.in.RegistrarParcelaCommand;
import com.agrocontrol.plot.domain.port.in.RegistrarParcelaUseCase;
import com.agrocontrol.plot.domain.port.out.ParcelaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso de parcelas.
 * Las reglas de NEGOCIO viven aqui (predio existente, codigo no repetido);
 * las de FORMATO viven en el Request DTO; las de INTEGRIDAD, en PostgreSQL.
 */
@Service
public class ParcelaService implements RegistrarParcelaUseCase, ConsultarParcelaUseCase,
        ActualizarParcelaUseCase, EliminarParcelaUseCase {

    private final ParcelaRepositoryPort repositoryPort;
    private final ConsultarPredioUseCase consultarPredioUseCase;

    public ParcelaService(ParcelaRepositoryPort repositoryPort,
                          ConsultarPredioUseCase consultarPredioUseCase) {
        this.repositoryPort = repositoryPort;
        this.consultarPredioUseCase = consultarPredioUseCase;
    }

    /**
     * Limite transaccional: las dos verificaciones y el INSERT forman UNA sola operacion.
     * Si cualquier paso lanza una excepcion, la transaccion se deshace y la tabla parcela
     * queda exactamente como estaba: no hay parcelas a medio guardar.
     */
    @Override
    @Transactional
    public Parcela registrar(RegistrarParcelaCommand command) {
        // 1. Validar que exista el predio padre (FK parcela.predio_id).
        verificarPredio(command.predioId());

        // 2. Validar el conflicto de negocio: codigo repetido en el mismo predio.
        String codigo = Parcela.normalizarCodigo(command.codigo());
        if (repositoryPort.existePorPredioIdYCodigo(command.predioId(), codigo)) {
            throw new CodigoParcelaDuplicadoException(codigo, command.predioId());
        }

        // 3. Construir el dominio (sus propias reglas, como superficie > 0, se validan aqui).
        Parcela parcela = Parcela.nueva(command.predioId(), command.codigo(), command.nombre(),
                command.superficie(), command.unidadSuperficie(), command.ubicacionReferencial());

        // 4. Guardar por el Port OUT.
        return repositoryPort.guardar(parcela);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Parcela> buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parcela> listar() {
        return repositoryPort.listarTodas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parcela> listarPorPredio(Long predioId) {
        verificarPredio(predioId);
        return repositoryPort.listarPorPredioId(predioId);
    }

    /**
     * PUT: tambien sirve para MOVER la parcela a otro predio (cambiar la FK predio_id).
     * Orden: 1) la parcela existe (404)  2) el predio destino existe (404)
     *        3) el codigo no esta en OTRA parcela de ese predio (409)  4) UPDATE.
     */
    @Override
    @Transactional
    public Parcela actualizar(Long id, ActualizarParcelaCommand command) {
        if (repositoryPort.buscarPorId(id).isEmpty()) {
            throw new ParcelaNoEncontradaException(id);
        }
        verificarPredio(command.predioId());

        String codigo = Parcela.normalizarCodigo(command.codigo());
        if (repositoryPort.existePorPredioIdYCodigoEnOtraParcela(command.predioId(), codigo, id)) {
            throw new CodigoParcelaDuplicadoException(codigo, command.predioId());
        }

        // Parcela CON id: JPA hace UPDATE (incluida la columna predio_id si cambio).
        Parcela actualizada = new Parcela(id, command.predioId(), command.codigo(), command.nombre(),
                command.superficie(), command.unidadSuperficie(), command.ubicacionReferencial());
        return repositoryPort.guardar(actualizada);
    }

    /**
     * DELETE: la parcela debe existir (404). Si esta en una campana,
     * el Port OUT lanza ParcelaEnUsoException (409) y no se borra nada.
     */
    @Override
    @Transactional
    public void eliminar(Long id) {
        Parcela parcela = repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ParcelaNoEncontradaException(id));
        repositoryPort.eliminar(parcela);
    }

    private void verificarPredio(Long predioId) {
        if (consultarPredioUseCase.buscarPorId(predioId).isEmpty()) {
            throw new PredioNoEncontradoException(predioId);
        }
    }
}
