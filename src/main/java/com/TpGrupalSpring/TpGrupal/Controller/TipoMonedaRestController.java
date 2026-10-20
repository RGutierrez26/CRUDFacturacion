package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda.TipoMonedaDTO;
import com.TpGrupalSpring.TpGrupal.Service.TipoMoneda.TipoMonedaServiceimpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-moneda")
public class TipoMonedaRestController {

    private final TipoMonedaServiceimpl tipoMonedaService;

    public TipoMonedaRestController(TipoMonedaServiceimpl tipoMonedaService) {
        this.tipoMonedaService = tipoMonedaService;
    }

    @GetMapping
    public List<TipoMonedaDTO> getAllTiposMoneda() {
        return tipoMonedaService.findAll();
    }
}
