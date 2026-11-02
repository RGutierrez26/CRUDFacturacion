package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecio.ListaPrecioDTO;
import com.TpGrupalSpring.TpGrupal.Service.ListaPrecio.ListaPrecioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/listaprecios", "/api/listas-precio"})
public class ListaPrecioRestController {

    private final ListaPrecioService listaPrecioService;

    public ListaPrecioRestController(ListaPrecioService listaPrecioService) {
        this.listaPrecioService = listaPrecioService;
    }

    @GetMapping
    public List<ListaPrecioDTO> getAll() {
        return listaPrecioService.findAll();
    }

    @GetMapping("/{id}")
    public ListaPrecioDTO getById(@PathVariable Long id) {
        return listaPrecioService.findById(id);
    }
}
