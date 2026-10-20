package com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.PuntoVenta;

public record PuntoVentaDTO(
        Long id,
        int numero,
        String descripcion,
        String tipoEmision,
        String domicilioComercial
) {
    public static PuntoVentaDTO toDTO(PuntoVenta pv) {
        if (pv == null) return null;
        return new PuntoVentaDTO(
                pv.getId(),
                pv.getNumero(),
                pv.getDescripcion(),
                pv.getTipoEmision(),
                pv.getDomicilioComercial()
        );
    }
}
