package com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta;

import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.FacturaVentaDetalle;

public record FacturaVentaDetalleDTO(
        Long id,
        ListaPrecioArticuloDTO listaPrecioArticulo,
        String descripcion,
        double cantidad,
        double precioUnitario,
        double porcentajeBonificacion,
        double importeNeto,
        double importeIva,
        double importeSubtotal
) {
    public static FacturaVentaDetalleDTO toDTO(FacturaVentaDetalle d) {
        if (d == null) return null;
        ListaPrecioArticuloDTO lpaDTO = d.getListaPrecioArticulo() != null
                ? ListaPrecioArticuloDTO.toDTO(d.getListaPrecioArticulo())
                : null;
        return new FacturaVentaDetalleDTO(
                d.getId(),
                lpaDTO,
                d.getDescripcion(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getPorcentajeBonificacion(),
                d.getImporteNeto(),
                d.getImporteIva(),
                d.getImporteSubtotal()
        );
    }
}
