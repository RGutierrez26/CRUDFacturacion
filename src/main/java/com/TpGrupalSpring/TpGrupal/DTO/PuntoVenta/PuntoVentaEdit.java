package com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.PuntoVenta;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

import java.util.Date;

public record PuntoVentaEdit(
        int numero,
        String descripcion,
        String tipoEmision,
        String domicilioComercial,
        Long usuarioModificacionId
) {
    public void applyTo(PuntoVenta pv, Usuario usuarioMod) {
        if (this.numero != 0) {
            pv.setNumero(this.numero);
        }
        if (this.descripcion != null) {
            pv.setDescripcion(this.descripcion);
        }
        if (this.tipoEmision != null) {
            pv.setTipoEmision(this.tipoEmision);
        }
        if (this.domicilioComercial != null) {
            pv.setDomicilioComercial(this.domicilioComercial);
        }
        if (usuarioMod != null) {
            pv.setFechaModificacion(usuarioMod);
        } else {
            pv.setFechaModificacion(new Date());
        }
    }
}
