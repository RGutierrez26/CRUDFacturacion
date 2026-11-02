package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.Rubro.RubroDTO;
import com.TpGrupalSpring.TpGrupal.Service.Rubro.RubroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rubros")
public class RubroRestController {

    private final RubroService rubroService;

    public RubroRestController(RubroService rubroService) {
        this.rubroService = rubroService;
    }

    @GetMapping
    public List<RubroDTO> getAll() {
        return rubroService.findAll();
    }

    @GetMapping("/{id}")
    public RubroDTO getById(@PathVariable Long id) {
        return rubroService.findById(id);
    }
}
