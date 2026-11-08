package com.agrocontrol.farm.infrastructure.adapter.in.web;

import com.agrocontrol.farm.domain.exception.PredioNoEncontradoException;
import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.domain.port.in.ActualizarPredioUseCase;
import com.agrocontrol.farm.domain.port.in.ConsultarPredioUseCase;
import com.agrocontrol.farm.domain.port.in.EliminarPredioUseCase;
import com.agrocontrol.farm.domain.port.in.RegistrarPredioUseCase;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.ActualizarPredioRequest;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.CrearPredioRequest;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.PredioResponse;
import com.agrocontrol.farm.infrastructure.adapter.in.web.mapper.PredioWebMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adapter IN web del modulo farm (Predio): CRUD completo (Guia 06 del frontend).
 * Depende solo de los Port IN, nunca de JpaRepository ni de la clase concreta PredioService.
 * No tiene try/catch: los errores los traduce GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/farms")
public class PredioController {

    private final RegistrarPredioUseCase registrar;
    private final ConsultarPredioUseCase consultar;
    private final ActualizarPredioUseCase actualizar;
    private final EliminarPredioUseCase eliminar;

    public PredioController(RegistrarPredioUseCase registrar,
                            ConsultarPredioUseCase consultar,
                            ActualizarPredioUseCase actualizar,
                            EliminarPredioUseCase eliminar) {
        this.registrar = registrar;
        this.consultar = consultar;
        this.actualizar = actualizar;
        this.eliminar = eliminar;
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

    /** GET /api/farms/{id} -> 200 o 404 (con ApiError, igual que los demas errores). */
    @GetMapping("/{id}")
    public PredioResponse buscar(@PathVariable Long id) {
        return consultar.buscarPorId(id)
                .map(PredioWebMapper::toResponse)
                .orElseThrow(() -> new PredioNoEncontradoException(id));
    }

    /** PUT /api/farms/{id} -> 200 con el predio actualizado, 400, 404 o 409 (nombre de otro predio). */
    @PutMapping("/{id}")
    public PredioResponse actualizar(@PathVariable Long id,
                                     @Valid @RequestBody ActualizarPredioRequest request) {
        Predio actualizado = actualizar.actualizar(id, PredioWebMapper.toDomain(request));
        return PredioWebMapper.toResponse(actualizado);
    }

    /** DELETE /api/farms/{id} -> 204 sin cuerpo, 404 o 409 (todavia tiene parcelas). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminar.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
