package com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta;

import java.util.Date;
import java.util.List;

public record FacturaRequestDTO(
        Long puntoVentaId,
        Long clienteId,
        Long condicionIvaId,
        Long tipoMonedaId,
        Date fechaEmision,
        String observaciones,
        Long usuarioCargaId,
        List<FacturaDetalleRequestDTO> detalles
) {
}
