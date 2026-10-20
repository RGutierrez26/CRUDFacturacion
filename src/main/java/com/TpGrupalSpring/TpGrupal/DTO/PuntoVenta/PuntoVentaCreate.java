package com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.PuntoVenta;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

public record PuntoVentaCreate(
        int numero,
        String descripcion,
        String tipoEmision,
        String domicilioComercial,
        Long usuarioCargaId
) {
    public PuntoVenta ToEntity(Usuario usuarioCarga) {
        return new PuntoVenta(
                this.numero,
                this.descripcion,
                this.tipoEmision,
                this.domicilioComercial,
                usuarioCarga
        );
    }
}
