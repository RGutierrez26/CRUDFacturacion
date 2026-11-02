package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReporteDTO;
import com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReportePageDTO;
import com.TpGrupalSpring.TpGrupal.Service.ReporteFacturaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

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

    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo) throws Exception {

        List<FacturaReporteDTO> facturas = reporteFacturaService.ObtenerFacturas(fechaDesde, fechaHasta, estado, montoMinimo);
        byte[] pdfBytes = reporteFacturaService.generarPdfBytes(facturas);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("reporte_facturas.pdf").build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double montoMinimo) throws Exception {

        List<FacturaReporteDTO> facturas = reporteFacturaService.ObtenerFacturas(fechaDesde, fechaHasta, estado, montoMinimo);
        byte[] excelBytes = reporteFacturaService.generarExcelTSVBytes(facturas);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel;charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("reporte_facturas.xls").build());

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}
