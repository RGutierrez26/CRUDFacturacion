package com.TpGrupalSpring.TpGrupal.DTO.CondicionIva;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.CondicionIva;

public record CondicionIvaDTO(
        Long id,
        int codigoAfip,
        String denominacion
) {
    public static CondicionIvaDTO toDTO(CondicionIva ci) {
        if (ci == null) return null;
        return new CondicionIvaDTO(
                ci.getId(),
                ci.getCodigoAfip(),
                ci.getDenominacion()
        );
    }
}
