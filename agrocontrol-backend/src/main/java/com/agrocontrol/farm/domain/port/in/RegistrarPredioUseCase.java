package com.agrocontrol.farm.domain.port.in;

import com.agrocontrol.farm.domain.model.Predio;

/**
 * Port IN: QUE puede hacer el sistema -> registrar un predio.
 * El controller depende de esta interfaz, no de la clase que la implementa.
 */
public interface RegistrarPredioUseCase {

    Predio registrar(Predio predio);
}
