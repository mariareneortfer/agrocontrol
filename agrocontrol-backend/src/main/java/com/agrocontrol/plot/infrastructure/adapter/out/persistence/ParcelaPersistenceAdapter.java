package com.agrocontrol.plot.infrastructure.adapter.out.persistence;

import com.agrocontrol.farm.infrastructure.adapter.out.persistence.repository.SpringDataPredioRepository;
import com.agrocontrol.plot.domain.exception.ParcelaEnUsoException;
import com.agrocontrol.plot.domain.model.Parcela;
import com.agrocontrol.plot.domain.port.out.ParcelaRepositoryPort;
import com.agrocontrol.plot.infrastructure.adapter.out.persistence.mapper.ParcelaPersistenceMapper;
import com.agrocontrol.plot.infrastructure.adapter.out.persistence.repository.SpringDataParcelaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter OUT de parcelas: implementa ParcelaRepositoryPort con JPA/PostgreSQL.
 * Usa el repositorio de predios SOLO para obtener la referencia que exige @ManyToOne;
 * esa dependencia queda dentro de infraestructura y el caso de uso no la ve.
 */
@Component
public class ParcelaPersistenceAdapter implements ParcelaRepositoryPort {

    private final SpringDataParcelaRepository repository;
    private final SpringDataPredioRepository predioRepository;

    public ParcelaPersistenceAdapter(SpringDataParcelaRepository repository,
                                     SpringDataPredioRepository predioRepository) {
        this.repository = repository;
        this.predioRepository = predioRepository;
    }

    @Override
    public Parcela guardar(Parcela parcela) {
        // getReferenceById no consulta la base: crea un "apuntador" al predio con ese id.
        var predioRef = predioRepository.getReferenceById(parcela.getPredioId());
        var entity = ParcelaPersistenceMapper.toJpa(parcela, predioRef);
        return ParcelaPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Parcela> buscarPorId(Long id) {
        return repository.findById(id)
                .map(ParcelaPersistenceMapper::toDomain);
    }

    @Override
    public List<Parcela> listarTodas() {
        return repository.findAllByOrderByIdAsc().stream()
                .map(ParcelaPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Parcela> listarPorPredioId(Long predioId) {
        return repository.findByPredio_IdOrderByIdAsc(predioId).stream()
                .map(ParcelaPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existePorPredioIdYCodigo(Long predioId, String codigo) {
        return repository.existsByPredio_IdAndCodigo(predioId, codigo);
    }

    @Override
    public boolean existePorPredioIdYCodigoEnOtraParcela(Long predioId, String codigo, Long idExcluido) {
        return repository.existsByPredio_IdAndCodigoAndIdNot(predioId, codigo, idExcluido);
    }

    @Override
    public void eliminar(Parcela parcela) {
        try {
            repository.deleteById(parcela.getId());
            // flush() ejecuta el DELETE ahora, asi una violacion de FK aparece aqui.
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            // campana_parcela todavia apunta a esta parcela.
            throw new ParcelaEnUsoException(parcela.getCodigo());
        }
    }
}
