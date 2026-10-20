package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.Cliente.ClienteDTO;
import com.TpGrupalSpring.TpGrupal.Service.Cliente.ClienteServiceimpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    private final ClienteServiceimpl clienteService;

    public ClienteRestController(ClienteServiceimpl clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteDTO> getAllClientes() {
        return clienteService.findAll();
    }

}
