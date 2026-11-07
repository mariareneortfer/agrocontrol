package com.agrocontrol.farm.infrastructure.adapter.in.web;

import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.domain.port.in.ConsultarPredioUseCase;
import com.agrocontrol.farm.domain.port.in.RegistrarPredioUseCase;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.CrearPredioRequest;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.PredioResponse;
import com.agrocontrol.farm.infrastructure.adapter.in.web.mapper.PredioWebMapper;
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
 * Adapter IN web del modulo farm (Predio).
 * Depende de los Port IN (RegistrarPredioUseCase, ConsultarPredioUseCase),
 * nunca de JpaRepository ni de la clase concreta PredioService.
 */
@RestController
@RequestMapping("/api/farms")
public class PredioController {

    private final RegistrarPredioUseCase registrar;
    private final ConsultarPredioUseCase consultar;

    public PredioController(RegistrarPredioUseCase registrar, ConsultarPredioUseCase consultar) {
        this.registrar = registrar;
        this.consultar = consultar;
    }

    /** POST /api/farms -> 201, 400 (validacion) o 409 (nombre repetido). */
    @PostMapping
    public ResponseEntity<PredioResponse> crear(@Valid @RequestBody CrearPredioRequest request) {
        Predio registrado = registrar.registrar(PredioWebMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PredioWebMapper.toResponse(registrado));
    }

    /** GET /api/farms o /api/farms?nombre=isidro -> 200. */
    @GetMapping
    public List<PredioResponse> listar(@RequestParam(required = false) String nombre) {
        return consultar.listar(nombre).stream()
                .map(PredioWebMapper::toResponse)
                .toList();
    }

    /** GET /api/farms/{id} -> 200 o 404. */
    @GetMapping("/{id}")
    public ResponseEntity<PredioResponse> buscar(@PathVariable Long id) {
        Optional<Predio> predio = consultar.buscarPorId(id);
        if (predio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PredioWebMapper.toResponse(predio.get()));
    }
}
