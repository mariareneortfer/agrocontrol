package com.agrocontrol.shared.application;

import org.springframework.stereotype.Service;

/**
 * Primer Bean de aplicacion. @Service le dice a Spring que cree UNA instancia
 * de esta clase y la entregue a quien la necesite (inyeccion de dependencias).
 * No tiene logica de persistencia.
 */
@Service
public class ProjectInfoService {

    public String projectName() {
        return "AgroControl";
    }

    public String backendName() {
        return "agrocontrol-backend";
    }

    public String backendStage() {
        return "CRUD_PREDIO";
    }
}
