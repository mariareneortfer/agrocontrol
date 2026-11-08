package com.agrocontrol.plot.domain.port.in;

import com.agrocontrol.plot.domain.model.Parcela;

/** Port IN: actualizar una parcela, incluido cambiarla de predio. */
public interface ActualizarParcelaUseCase {

    Parcela actualizar(Long id, ActualizarParcelaCommand command);
}
