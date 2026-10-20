package com.TpGrupalSpring.TpGrupal.DTO.CondicionIva;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.CondicionIva;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

public record CondicionIvaCreate(
        int codigoAfip,
        String denominacion,
        Long usuarioCargaId
) {
    public CondicionIva ToEntity(Usuario usuarioCarga) {
        return new CondicionIva(
                this.codigoAfip,
                this.denominacion,
                usuarioCarga
        );
    }
}
