package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.Articulo.ArticuloDTO;
import com.TpGrupalSpring.TpGrupal.Service.Articulo.ArticuloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/articulos")
public class ArticuloRestController {

    private final ArticuloService articuloService;

    public ArticuloRestController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @GetMapping
    public List<ArticuloDTO> getAll() {
        return articuloService.findAll();
    }

    @GetMapping("/{id}")
    public ArticuloDTO getById(@PathVariable Long id) {
        return articuloService.findById(id);
    }
}
