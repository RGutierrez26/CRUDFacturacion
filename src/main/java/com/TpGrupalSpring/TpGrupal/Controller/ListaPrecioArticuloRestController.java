package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecioArticulo;
import com.TpGrupalSpring.TpGrupal.Service.ListaPrecioArticulo.ListaPrecioArticuloServiceimpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listas-precio-articulos")
public class ListaPrecioArticuloRestController {

    private final ListaPrecioArticuloServiceimpl listaPrecioArticuloService;

    public ListaPrecioArticuloRestController(ListaPrecioArticuloServiceimpl listaPrecioArticuloService) {
        this.listaPrecioArticuloService = listaPrecioArticuloService;
    }

    @GetMapping
    public List<ListaPrecioArticuloDTO> getListaPrecioArticulo() {
        return listaPrecioArticuloService.findAll();
    }
}
