package com.agrocontrol.plot.infrastructure.adapter.in.web.mapper;

import com.agrocontrol.plot.domain.model.Parcela;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.CrearParcelaRequest;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.ParcelaResponse;

/** Traduce entre los DTO de la API y el modelo de dominio de parcelas. */
public final class ParcelaWebMapper {

    private ParcelaWebMapper() {
    }

    public static Parcela toDomain(CrearParcelaRequest r) {
        return Parcela.nueva(r.predioId(), r.codigo(), r.nombre(), r.superficie(),
                r.unidadSuperficie(), r.ubicacionReferencial());
    }

    public static ParcelaResponse toResponse(Parcela p) {
        return new ParcelaResponse(p.getId(), p.getPredioId(), p.getCodigo(), p.getNombre(),
                p.getSuperficie(), p.getUnidadSuperficie(), p.getUbicacionReferencial());
    }
}
