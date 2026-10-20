package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.CondicionIva.CondicionIvaDTO;
import com.TpGrupalSpring.TpGrupal.Service.CondicionIva.CondicionIvaServiceimpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/condiciones-iva")
public class CondicionIvaRestController {

    private final CondicionIvaServiceimpl condicionIvaService;

    public CondicionIvaRestController(CondicionIvaServiceimpl condicionIvaService) {
        this.condicionIvaService = condicionIvaService;
    }

    @GetMapping
    public List<CondicionIvaDTO> getAllCondicionesIva() {
        return condicionIvaService.findAll();
    }
}
