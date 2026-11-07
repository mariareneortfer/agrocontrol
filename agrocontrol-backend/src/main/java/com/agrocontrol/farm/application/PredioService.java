package com.agrocontrol.farm.application;

import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.infrastructure.adapter.out.persistence.entity.PredioJpaEntity;
import com.agrocontrol.farm.infrastructure.adapter.out.persistence.mapper.PredioPersistenceMapper;
import com.agrocontrol.farm.infrastructure.adapter.out.persistence.repository.SpringDataPredioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Casos de uso de predios, ahora sobre PostgreSQL (tabla agrocontrol.predio).
 * En este capitulo el servicio usa directamente el repositorio Spring Data.
 * En el Capitulo 06 dependera de un puerto (interfaz) y no de Spring Data.
 */
@Service
public class PredioService {

    private final SpringDataPredioRepository repository;

    public PredioService(SpringDataPredioRepository repository) {
        this.repository = repository;
    }

    /** INSERT en agrocontrol.predio. PostgreSQL genera el predio_id. */
    public Predio registrar(String nombre, String ubicacion, String descripcion) {
        PredioJpaEntity guardado = repository.save(new PredioJpaEntity(nombre, ubicacion, descripcion));
        return PredioPersistenceMapper.toDomain(guardado);
    }

    public List<Predio> listar() {
        return repository.findAll(Sort.by("id")).stream()
                .map(PredioPersistenceMapper::toDomain)
                .toList();
    }

    public List<Predio> listarPorNombre(String texto) {
        return repository.findByNombreContainingIgnoreCaseOrderByIdAsc(texto).stream()
                .map(PredioPersistenceMapper::toDomain)
                .toList();
    }

    public Optional<Predio> buscarPorId(Long id) {
        return repository.findById(id)
                .map(PredioPersistenceMapper::toDomain);
    }
}
