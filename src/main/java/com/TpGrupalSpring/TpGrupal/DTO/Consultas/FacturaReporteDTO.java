package com.TpGrupalSpring.TpGrupal.DTO.Consultas;

import java.util.Date;

public record FacturaReporteDTO(
        Long numeroFactura,
        Date fechaEmision,
        String ClienteDenominacion,
        String condicionIva,
        String puntoVentaDescripcion,
        double importeTotal,
        Long CantidadItemns,
        String estado
) {
}
