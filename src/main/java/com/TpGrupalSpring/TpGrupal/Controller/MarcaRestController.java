package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.Marca.MarcaDTO;
import com.TpGrupalSpring.TpGrupal.Service.Marca.MarcaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/marcas")
public class MarcaRestController {

    private final MarcaService marcaService;

    public MarcaRestController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @GetMapping
    public List<MarcaDTO> getAll() {
        return marcaService.findAll();
    }

    @GetMapping("/{id}")
    public MarcaDTO getById(@PathVariable Long id) {
        return marcaService.findById(id);
    }
}
