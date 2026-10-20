package com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.TipoMoneda;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

public record TipoMonedaCreate(
        String codigoAfip,
        String denominacion,
        String simbolo,
        Long usuarioCargaId
) {
    public TipoMoneda ToEntity(Usuario usuarioCarga) {
        return new TipoMoneda(
                this.codigoAfip,
                this.denominacion,
                this.simbolo,
                usuarioCarga
        );
    }
}
