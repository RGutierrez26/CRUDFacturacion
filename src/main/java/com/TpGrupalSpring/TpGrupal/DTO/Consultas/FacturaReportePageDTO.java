package com.TpGrupalSpring.TpGrupal.DTO.Consultas;

import java.util.List;

public record FacturaReportePageDTO(
        List<FacturaReporteDTO> facturas,
        int paginaActual,
        int totalPaginas,
        long totalElementos,
        int tamanioPagina
) {
}
