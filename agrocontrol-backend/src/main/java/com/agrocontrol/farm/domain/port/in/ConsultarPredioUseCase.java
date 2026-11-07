package com.agrocontrol.farm.domain.port.in;

import com.agrocontrol.farm.domain.model.Predio;

import java.util.List;
import java.util.Optional;

/** Port IN: QUE puede hacer el sistema -> consultar predios. */
public interface ConsultarPredioUseCase {

    Optional<Predio> buscarPorId(Long id);

    /** Si filtroNombre es null o vacio, devuelve todos los predios. */
    List<Predio> listar(String filtroNombre);
}
