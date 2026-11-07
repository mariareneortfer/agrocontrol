package com.agrocontrol.farm.infrastructure.web;

import com.agrocontrol.farm.application.PredioDemoResponse;
import com.agrocontrol.farm.application.PredioDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint demo de la entidad padre Predio: GET /api/farms/demo
 * Se usa "farms" (en ingles) para seguir el estilo de la API de la ficha: /plots, /campaigns, /tasks.
 */
@RestController
@RequestMapping("/api/farms")
public class PredioDemoController {

    private final PredioDemoService service;

    // Inyeccion por constructor del Bean PredioDemoService.
    public PredioDemoController(PredioDemoService service) {
        this.service = service;
    }

    @GetMapping("/demo")
    public PredioDemoResponse demo() {
        return service.obtenerDemo();
    }
}
