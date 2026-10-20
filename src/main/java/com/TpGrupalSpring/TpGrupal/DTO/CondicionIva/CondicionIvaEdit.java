package com.TpGrupalSpring.TpGrupal.DTO.CondicionIva;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.CondicionIva;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

import java.util.Date;

public record CondicionIvaEdit(
        int codigoAfip,
        String denominacion,
        Long usuarioModificacionId
) {
    public void applyTo(CondicionIva ci, Usuario usuarioMod) {
        if (this.codigoAfip != 0) {
            ci.setCodigoAfip(this.codigoAfip);
        }
        if (this.denominacion != null) {
            ci.setDenominacion(this.denominacion);
        }
        if (usuarioMod != null) {
            ci.setFechaModificacion(usuarioMod);
        } else {
            ci.setFechaModificacion(new Date());
        }
    }
}
