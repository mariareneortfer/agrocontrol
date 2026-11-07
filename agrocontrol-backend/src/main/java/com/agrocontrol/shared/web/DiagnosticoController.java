package com.agrocontrol.shared.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SOLO PARA EL LABORATORIO DEL CAPITULO 08.
 * Simula un error no controlado para comprobar que GlobalExceptionHandler
 * responde 500 con ApiError, sin mostrar detalles internos.
 * Debe eliminarse antes de pasar el backend a produccion.
 */
@RestController
@RequestMapping("/api/diagnostico")
public class DiagnosticoController {

    @GetMapping("/error-simulado")
    public void errorSimulado() {
        throw new IllegalStateException("Error simulado: detalle interno que el cliente NO debe ver");
    }
}
