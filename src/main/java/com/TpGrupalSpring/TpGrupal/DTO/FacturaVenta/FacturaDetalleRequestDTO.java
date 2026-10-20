package com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta;

public record FacturaDetalleRequestDTO(
        Long listaPrecioArticuloId,
        String descripcion,
        double cantidad,
        double precioUnitario,
        double porcentajeBonificacion
) {
}
