package com.agrocontrol.farm.infrastructure.adapter.in.web.mapper;

import com.agrocontrol.farm.domain.model.Predio;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.CrearPredioRequest;
import com.agrocontrol.farm.infrastructure.adapter.in.web.dto.PredioResponse;

/** Traduce entre los DTO de la API y el modelo de dominio. */
public final class PredioWebMapper {

    private PredioWebMapper() {
    }

    public static Predio toDomain(CrearPredioRequest request) {
        return Predio.nuevo(request.nombre(), request.ubicacion(), request.descripcion());
    }

    public static PredioResponse toResponse(Predio predio) {
        return new PredioResponse(
                predio.getId(),
                predio.getNombre(),
                predio.getUbicacion(),
                predio.getDescripcion());
    }
}
