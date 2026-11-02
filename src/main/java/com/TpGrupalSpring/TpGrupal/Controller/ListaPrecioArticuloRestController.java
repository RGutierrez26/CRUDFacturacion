package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloDTO;
import com.TpGrupalSpring.TpGrupal.Service.ListaPrecioArticulo.ListaPrecioArticuloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listas-precio-articulos")
public class ListaPrecioArticuloRestController {

    private final ListaPrecioArticuloService listaPrecioArticuloService;

    public ListaPrecioArticuloRestController(ListaPrecioArticuloService listaPrecioArticuloService) {
        this.listaPrecioArticuloService = listaPrecioArticuloService;
    }

    @GetMapping
    public List<ListaPrecioArticuloDTO> getAll() {
        return listaPrecioArticuloService.findAll();
    }

    @GetMapping("/listas-precio/{listaPrecioId}")
    public List<ListaPrecioArticuloDTO> getByListaPrecioId(@PathVariable Long listaPrecioId) {
        return listaPrecioArticuloService.findByListaPrecioId(listaPrecioId);
    }
}
