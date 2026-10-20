package com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.TipoMoneda;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

import java.util.Date;

public record TipoMonedaEdit(
        String codigoAfip,
        String denominacion,
        String simbolo,
        Long usuarioModificacionId
) {
    public void applyTo(TipoMoneda tm, Usuario usuarioMod) {
        if (this.codigoAfip != null) {
            tm.setCodigoAfip(this.codigoAfip);
        }
        if (this.denominacion != null) {
            tm.setDenominacion(this.denominacion);
        }
        if (this.simbolo != null) {
            tm.setSimbolo(this.simbolo);
        }
        if (usuarioMod != null) {
            tm.setFechaModificacion(usuarioMod);
        } else {
            tm.setFechaModificacion(new Date());
        }
    }
}
