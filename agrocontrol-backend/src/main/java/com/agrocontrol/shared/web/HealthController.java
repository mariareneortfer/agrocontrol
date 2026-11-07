package com.agrocontrol.shared.web;

import com.agrocontrol.shared.application.ProjectInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de salud: GET /api/health
 * Responde JSON con HTTP 200 si la aplicacion esta funcionando.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final ProjectInfoService projectInfoService;

    // Inyeccion por constructor: Spring entrega el Bean ProjectInfoService.
    // Nunca se escribe "new ProjectInfoService()" aqui.
    public HealthController(ProjectInfoService projectInfoService) {
        this.projectInfoService = projectInfoService;
    }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("status", "OK");
        respuesta.put("application", projectInfoService.backendName());
        respuesta.put("project", projectInfoService.projectName());
        respuesta.put("stage", projectInfoService.backendStage());
        respuesta.put("timestamp", Instant.now().toString());
        return respuesta;
    }
}
