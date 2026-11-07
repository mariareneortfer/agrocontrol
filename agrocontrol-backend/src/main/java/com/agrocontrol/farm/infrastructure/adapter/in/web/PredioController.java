package com.agrocontrol.farm.infrastructure.adapter.in.web;

import com.agrocontrol.farm.application.PredioService;
import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.CrearPredioRequest;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.PredioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de entrada web del modulo farm (Predio).
 * Recibe HTTP, delega en PredioService y responde con DTOs.
 * Se usa /api/farms para seguir el estilo de la API de la ficha (/plots, /campaigns).
 */
@RestController
@RequestMapping("/api/farms")
public class PredioController {

    private final PredioService service;

    public PredioController(PredioService service) {
        this.service = service;
    }

    /** POST /api/farms -> 201 Created, o 400 si el JSON no es valido. */
    @PostMapping
    public ResponseEntity<PredioResponse> crear(@Valid @RequestBody CrearPredioRequest request) {
        Predio predio = service.registrar(request.nombre(), request.ubicacion(), request.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(predio));
    }

    /** GET /api/farms o /api/farms?nombre=isidro -> 200 con la lista. */
    @GetMapping
    public List<PredioResponse> listar(@RequestParam(required = false) String nombre) {
        List<Predio> predios = (nombre == null || nombre.isBlank())
                ? service.listar()
                : service.listarPorNombre(nombre);
        return predios.stream()
                .map(PredioController::toResponse)
                .toList();
    }

    /** GET /api/farms/{id} -> 200 si existe, 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<PredioResponse> buscarPorId(@PathVariable Long id) {
        Optional<Predio> predio = service.buscarPorId(id);
        if (predio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(predio.get()));
    }

    /** Convierte la clase de dominio en el DTO de salida. */
    private static PredioResponse toResponse(Predio predio) {
        return new PredioResponse(
                predio.getId(),
                predio.getNombre(),
                predio.getUbicacion(),
                predio.getDescripcion());
    }
}
