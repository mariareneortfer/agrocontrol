package com.agrocontrol.plot.domain.port.in;

import com.agrocontrol.plot.domain.model.Parcela;

/** Port IN: registrar una parcela dentro de un predio existente. */
public interface RegistrarParcelaUseCase {

    Parcela registrar(RegistrarParcelaCommand command);
}
