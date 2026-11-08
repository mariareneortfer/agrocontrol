package com.agrocontrol.plot.infrastructure.adapter.in.web;

import com.agrocontrol.plot.domain.model.Parcela;
import com.agrocontrol.plot.domain.port.in.ConsultarParcelaUseCase;
import com.agrocontrol.plot.domain.port.in.RegistrarParcelaUseCase;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.CrearParcelaRequest;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.ParcelaResponse;
import com.agrocontrol.plot.infrastructure.adapter.in.web.mapper.ParcelaWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Adapter IN web de parcelas. /api/plots es la ruta que usa la API de la ficha del proyecto.
 * Depende solo de los Port IN del modulo plot. No tiene try/catch:
 * los errores los traduce GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/plots")
public class ParcelaController {

    private final RegistrarParcelaUseCase registrar;
    private final ConsultarParcelaUseCase consultar;

    public ParcelaController(RegistrarParcelaUseCase registrar, ConsultarParcelaUseCase consultar) {
        this.registrar = registrar;
        this.consultar = consultar;
    }

    /** POST /api/plots -> 201, 400 (validacion), 404 (predio no existe) o 409 (codigo repetido). */
    @PostMapping
    public ResponseEntity<ParcelaResponse> crear(@Valid @RequestBody CrearParcelaRequest request) {
        Parcela registrada = registrar.registrar(ParcelaWebMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ParcelaWebMapper.toResponse(registrada));
    }

    /** GET /api/plots -> 200 con todas las parcelas (lo usa el listado del frontend, Guia 05). */
    @GetMapping
    public List<ParcelaResponse> listar() {
        return consultar.listar().stream()
                .map(ParcelaWebMapper::toResponse)
                .toList();
    }

    /** GET /api/plots/{id} -> 200 o 404. */
    @GetMapping("/{id}")
    public ResponseEntity<ParcelaResponse> buscar(@PathVariable Long id) {
        Optional<Parcela> parcela = consultar.buscarPorId(id);
        if (parcela.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ParcelaWebMapper.toResponse(parcela.get()));
    }

    /** GET /api/plots/farm/{predioId} -> 200 con las parcelas del predio, o 404 si el predio no existe. */
    @GetMapping("/farm/{predioId}")
    public List<ParcelaResponse> listarPorPredio(@PathVariable Long predioId) {
        return consultar.listarPorPredio(predioId).stream()
                .map(ParcelaWebMapper::toResponse)
                .toList();
    }
}
