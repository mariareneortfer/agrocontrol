package com.agrocontrol.plot.infrastructure.adapter.in.web;

import com.agrocontrol.plot.domain.exception.ParcelaNoEncontradaException;
import com.agrocontrol.plot.domain.model.Parcela;
import com.agrocontrol.plot.domain.port.in.ActualizarParcelaUseCase;
import com.agrocontrol.plot.domain.port.in.ConsultarParcelaUseCase;
import com.agrocontrol.plot.domain.port.in.EliminarParcelaUseCase;
import com.agrocontrol.plot.domain.port.in.RegistrarParcelaUseCase;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.ActualizarParcelaRequest;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.CrearParcelaRequest;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.ParcelaResponse;
import com.agrocontrol.plot.infrastructure.adapter.in.web.mapper.ParcelaWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adapter IN web de parcelas: CRUD completo (Guia 07 del frontend).
 * Depende solo de los Port IN del modulo plot. No tiene try/catch:
 * los errores los traduce GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/plots")
public class ParcelaController {

    private final RegistrarParcelaUseCase registrar;
    private final ConsultarParcelaUseCase consultar;
    private final ActualizarParcelaUseCase actualizar;
    private final EliminarParcelaUseCase eliminar;

    public ParcelaController(RegistrarParcelaUseCase registrar,
                             ConsultarParcelaUseCase consultar,
                             ActualizarParcelaUseCase actualizar,
                             EliminarParcelaUseCase eliminar) {
        this.registrar = registrar;
        this.consultar = consultar;
        this.actualizar = actualizar;
        this.eliminar = eliminar;
    }

    /** POST /api/plots -> 201, 400 (validacion), 404 (predio no existe) o 409 (codigo repetido). */
    @PostMapping
    public ResponseEntity<ParcelaResponse> crear(@Valid @RequestBody CrearParcelaRequest request) {
        Parcela registrada = registrar.registrar(ParcelaWebMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ParcelaWebMapper.toResponse(registrada));
    }

    /** GET /api/plots -> 200 con todas las parcelas. */
    @GetMapping
    public List<ParcelaResponse> listar() {
        return consultar.listar().stream()
                .map(ParcelaWebMapper::toResponse)
                .toList();
    }

    /** GET /api/plots/{id} -> 200 o 404 (con ApiError). */
    @GetMapping("/{id}")
    public ParcelaResponse buscar(@PathVariable Long id) {
        return consultar.buscarPorId(id)
                .map(ParcelaWebMapper::toResponse)
                .orElseThrow(() -> new ParcelaNoEncontradaException(id));
    }

    /**
     * PUT /api/plots/{id} -> 200 con la parcela actualizada.
     * 400 (validacion), 404 (parcela o predio destino no existen), 409 (codigo repetido en el predio destino).
     */
    @PutMapping("/{id}")
    public ParcelaResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarParcelaRequest request) {
        Parcela actualizada = actualizar.actualizar(id, ParcelaWebMapper.toCommand(request));
        return ParcelaWebMapper.toResponse(actualizada);
    }

    /** DELETE /api/plots/{id} -> 204, 404 o 409 (la parcela esta en una campana). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminar.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/plots/farm/{predioId} -> 200 con las parcelas del predio, o 404 si el predio no existe. */
    @GetMapping("/farm/{predioId}")
    public List<ParcelaResponse> listarPorPredio(@PathVariable Long predioId) {
        return consultar.listarPorPredio(predioId).stream()
                .map(ParcelaWebMapper::toResponse)
                .toList();
    }
}
