package com.agrocontrol.farm.domain.port.in;

/** Port IN: QUE puede hacer el sistema -> eliminar un predio. */
public interface EliminarPredioUseCase {

    void eliminar(Long id);
}
