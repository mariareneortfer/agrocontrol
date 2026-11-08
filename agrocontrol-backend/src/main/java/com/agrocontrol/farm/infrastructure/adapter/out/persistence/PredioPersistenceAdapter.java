package com.agrocontrol.farm.infrastructure.adapter.out.persistence;

import com.agrocontrol.farm.domain.exception.PredioConParcelasException;
import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.domain.port.out.PredioRepositoryPort;
import com.agrocontrol.farm.infrastructure.adapter.out.persistence.mapper.PredioPersistenceMapper;
import com.agrocontrol.farm.infrastructure.adapter.out.persistence.repository.SpringDataPredioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter OUT: implementa el Port OUT usando JPA/PostgreSQL.
 * Es la UNICA clase del modulo que conoce SpringDataPredioRepository.
 * Si se cambiara PostgreSQL por otra tecnologia, se reemplaza esta clase
 * (y la entidad JPA), pero el dominio y el caso de uso no cambian.
 */
@Component
public class PredioPersistenceAdapter implements PredioRepositoryPort {

    private final SpringDataPredioRepository repository;

    public PredioPersistenceAdapter(SpringDataPredioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Predio guardar(Predio predio) {
        var entity = PredioPersistenceMapper.toEntity(predio);
        var saved = repository.save(entity);
        return PredioPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Predio> buscarPorId(Long id) {
        return repository.findById(id)
                .map(PredioPersistenceMapper::toDomain);
    }

    @Override
    public List<Predio> listarTodos() {
        return repository.findAll(Sort.by("id")).stream()
                .map(PredioPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Predio> buscarPorNombreQueContiene(String texto) {
        return repository.findByNombreContainingIgnoreCaseOrderByIdAsc(texto).stream()
                .map(PredioPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return repository.existsByNombre(nombre);
    }

    @Override
    public boolean existePorNombreEnOtroPredio(String nombre, Long idExcluido) {
        return repository.existsByNombreAndIdNot(nombre, idExcluido);
    }

    @Override
    public void eliminar(Predio predio) {
        try {
            repository.deleteById(predio.getId());
            // flush() ejecuta el DELETE ahora mismo, asi el error de la FK aparece aqui y no al confirmar.
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            // fk_parcela_predio: todavia hay filas en parcela que apuntan a este predio.
            throw new PredioConParcelasException(predio.getNombre());
        }
    }
}
