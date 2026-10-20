package com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.TipoMoneda;

public record TipoMonedaDTO(
        Long id,
        String codigoAfip,
        String denominacion,
        String simbolo
) {
    public static TipoMonedaDTO toDTO(TipoMoneda tm) {
        if (tm == null) return null;
        return new TipoMonedaDTO(
                tm.getId(),
                tm.getCodigoAfip(),
                tm.getDenominacion(),
                tm.getSimbolo()
        );
    }
}
