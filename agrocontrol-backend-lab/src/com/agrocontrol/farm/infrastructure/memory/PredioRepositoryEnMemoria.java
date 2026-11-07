package com.agrocontrol.farm.infrastructure.memory;

import com.agrocontrol.farm.domain.Predio;
import com.agrocontrol.farm.domain.port.PredioRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacion temporal del contrato: guarda los predios en memoria.
 * Se usa Map porque la operacion principal es buscar por id (clave -> valor).
 * LinkedHashMap conserva el orden en que se registraron.
 * Cuando llegue PostgreSQL, se reemplaza esta clase y el servicio no cambia.
 */
public class PredioRepositoryEnMemoria implements PredioRepository {

    private final Map<Long, Predio> datos = new LinkedHashMap<>();

    @Override
    public Predio guardar(Predio predio) {
        datos.put(predio.getId(), predio);
        return predio;
    }

    @Override
    public Optional<Predio> buscarPorId(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Predio> listarTodos() {
        // Copia nueva: quien la reciba no puede alterar el Map interno.
        return new ArrayList<>(datos.values());
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return datos.values().stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
    }
}
