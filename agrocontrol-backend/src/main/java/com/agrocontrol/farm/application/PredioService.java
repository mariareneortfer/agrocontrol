package com.agrocontrol.farm.application;

import com.agrocontrol.farm.domain.Predio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio temporal EN MEMORIA. Los predios se guardan en una lista
 * y se pierden al reiniciar la aplicacion. En el proximo capitulo
 * se reemplazara por PostgreSQL (tabla predio).
 */
@Service
public class PredioService {

    private final List<Predio> predios = new ArrayList<>();

    // Genera ids 1, 2, 3... como lo hara la columna IDENTITY de predio_id.
    private final AtomicLong secuencia = new AtomicLong(0);

    public Predio registrar(String nombre, String ubicacion, String descripcion) {
        Predio predio = new Predio(secuencia.incrementAndGet(), nombre, ubicacion, descripcion);
        predios.add(predio);
        return predio;
    }

    public List<Predio> listar() {
        return List.copyOf(predios);
    }

    /** Filtro opcional: predios cuyo nombre contiene el texto, sin importar mayusculas. */
    public List<Predio> listarPorNombre(String texto) {
        String buscado = texto.toLowerCase();
        return predios.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(buscado))
                .toList();
    }

    public Optional<Predio> buscarPorId(Long id) {
        return predios.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }
}
