package com.agrocontrol.farm.application;

import org.springframework.stereotype.Service;

/**
 * Servicio temporal: devuelve un predio de ejemplo escrito en el codigo.
 * Todavia NO consulta PostgreSQL; eso llegara cuando se agregue la persistencia.
 */
@Service
public class PredioDemoService {

    public PredioDemoResponse obtenerDemo() {
        return new PredioDemoResponse(
                1L,
                "San Isidro",
                "Km 12 carretera al norte",
                2
        );
    }
}
