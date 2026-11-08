package com.agrocontrol.farm.domain.port.in;

import com.agrocontrol.farm.domain.model.Predio;

/** Port IN: QUE puede hacer el sistema -> actualizar los datos de un predio existente. */
public interface ActualizarPredioUseCase {

    /**
     * Reemplaza nombre, ubicacion y descripcion del predio con ese id.
     * @param datos predio sin id con los valores nuevos
     */
    Predio actualizar(Long id, Predio datos);
}
