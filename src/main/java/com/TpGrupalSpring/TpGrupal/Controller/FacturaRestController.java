package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReportePageDTO;
import com.TpGrupalSpring.TpGrupal.Service.ReporteFacturaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController()
@RequestMapping("/api/facturareportes")
public class FacturaRestController {
    private final ReporteFacturaService reporteFacturaService;

    public FacturaRestController(ReporteFacturaService reporteFacturaService) {
        this.reporteFacturaService = reporteFacturaService;
    }

    @GetMapping
    public FacturaReportePageDTO ObtenerFacturas(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) Double montoMinimo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return reporteFacturaService.ObtenerFacturas(fechaDesde, fechaHasta, estado, montoMinimo, page, size);
    }
}
