package com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta;

import java.util.Date;
import java.util.List;

public record FacturaRequestDTO(
        Long puntoVentaId,
        Long clienteId,
        Long condicionIvaId,
        Long tipoMonedaId,
        Long numero,
        Date fechaEmision,
        String estado,
        String observaciones,
        Long usuarioCargaId,
        List<FacturaDetalleRequestDTO> detalles
) {
}
