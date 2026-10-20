package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta.PuntoVentaDTO;
import com.TpGrupalSpring.TpGrupal.Service.PuntoVenta.PuntoVentaServiceimpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/puntos-venta")
public class PuntoVentaRestController {

    private final PuntoVentaServiceimpl puntoVentaService;

    public PuntoVentaRestController(PuntoVentaServiceimpl puntoVentaService) {
        this.puntoVentaService = puntoVentaService;
    }

    @GetMapping
    public List<PuntoVentaDTO> getAllPuntosVenta() {
        return puntoVentaService.findAll();
    }
}
