package com.agrocontrol.plot.infrastructure.adapter.in.web.mapper;

import com.agrocontrol.plot.domain.model.Parcela;
import com.agrocontrol.plot.domain.port.in.ActualizarParcelaCommand;
import com.agrocontrol.plot.domain.port.in.RegistrarParcelaCommand;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.ActualizarParcelaRequest;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.CrearParcelaRequest;
import com.agrocontrol.plot.infrastructure.adapter.in.web.dto.ParcelaResponse;

/** Traduce Request DTO -> Command y dominio -> Response DTO. */
public final class ParcelaWebMapper {

    private ParcelaWebMapper() {
    }

    public static RegistrarParcelaCommand toCommand(CrearParcelaRequest r) {
        return new RegistrarParcelaCommand(r.predioId(), r.codigo(), r.nombre(), r.superficie(),
                r.unidadSuperficie(), r.ubicacionReferencial());
    }

    public static ActualizarParcelaCommand toCommand(ActualizarParcelaRequest r) {
        return new ActualizarParcelaCommand(r.predioId(), r.codigo(), r.nombre(), r.superficie(),
                r.unidadSuperficie(), r.ubicacionReferencial());
    }

    public static ParcelaResponse toResponse(Parcela p) {
        return new ParcelaResponse(p.getId(), p.getPredioId(), p.getCodigo(), p.getNombre(),
                p.getSuperficie(), p.getUnidadSuperficie(), p.getUbicacionReferencial());
    }
}
