package com.agrocontrol.shared.web;

import com.agrocontrol.farm.domain.exception.PredioConParcelasException;
import com.agrocontrol.farm.domain.exception.PredioDuplicadoException;
import com.agrocontrol.farm.domain.exception.PredioNoEncontradoException;
import com.agrocontrol.plot.domain.exception.CodigoParcelaDuplicadoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Punto UNICO donde las excepciones se convierten en respuestas HTTP.
 * Gracias a esta clase, los controllers no necesitan try/catch:
 * si algo falla, la excepcion sube hasta aqui y se responde con ApiError.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 404: no existe lo que se pide ----------

    @ExceptionHandler(PredioNoEncontradoException.class)
    public ResponseEntity<ApiError> handlePredioNoEncontrado(PredioNoEncontradoException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleRutaInexistente(NoResourceFoundException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request, Map.of());
    }

    // ---------- 409: conflicto con datos existentes ----------

    @ExceptionHandler(PredioDuplicadoException.class)
    public ResponseEntity<ApiError> handlePredioDuplicado(PredioDuplicadoException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(PredioConParcelasException.class)
    public ResponseEntity<ApiError> handlePredioConParcelas(PredioConParcelasException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CodigoParcelaDuplicadoException.class)
    public ResponseEntity<ApiError> handleCodigoParcelaDuplicado(CodigoParcelaDuplicadoException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    /**
     * Ultima defensa: PostgreSQL rechazo la operacion (FK, UNIQUE o CHECK).
     * NO se devuelve el mensaje de PostgreSQL: revelaria nombres de tablas y restricciones internas.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegridad(DataIntegrityViolationException ex,
                                                     HttpServletRequest request) {
        log.warn("Violacion de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de integridad de los datos", request, Map.of());
    }

    // ---------- 400: la peticion esta mal formada ----------

    /** @Valid rechazo el Request DTO: se informa campo por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "La peticion tiene campos invalidos", request, fieldErrors);
    }

    /** El cuerpo no es un JSON valido o un valor no tiene el tipo esperado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonInvalido(HttpMessageNotReadableException ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido", request, Map.of());
    }

    /** Ejemplo: GET /api/plots/abc, cuando el id debe ser un numero. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getName() + "' tiene un valor invalido", request, Map.of());
    }

    /** Una regla del modelo de dominio rechazo los datos (por ejemplo, en un constructor). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleReglaDominio(IllegalArgumentException ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
    }

    // ---------- 405: metodo HTTP no permitido ----------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMetodo(HttpRequestMethodNotSupportedException ex,
                                                 HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo " + ex.getMethod() + " no esta permitido en esta ruta", request, Map.of());
    }

    // ---------- 500: cualquier error no previsto ----------

    /**
     * Red de seguridad. El detalle completo se escribe en el log del servidor;
     * al cliente solo se le da un mensaje generico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleInesperado(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno. Intente nuevamente o contacte al administrador", request, Map.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest request, Map<String, String> fieldErrors) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
