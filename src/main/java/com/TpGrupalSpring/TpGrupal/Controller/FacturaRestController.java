package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReporteDTO;
import com.TpGrupalSpring.TpGrupal.Service.ReporteFacturaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController()
@RequestMapping("/api/facturareportes")
public class FacturaRestController {
    ReporteFacturaService reporteFacturaService;

    public FacturaRestController(ReporteFacturaService reporteFacturaService) {
        this.reporteFacturaService = reporteFacturaService;
    }

    @GetMapping
    public List<FacturaReporteDTO> ObtenerFacturas(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) Double montoMinimo){
        return reporteFacturaService.ObtenerFacturas(fechaDesde, fechaHasta, estado, montoMinimo);
    }



}
