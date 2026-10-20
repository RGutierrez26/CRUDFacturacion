package com.TpGrupalSpring.TpGrupal.Service.Cliente;

import com.TpGrupalSpring.TpGrupal.DTO.Cliente.ClienteCreate;
import com.TpGrupalSpring.TpGrupal.DTO.Cliente.ClienteDTO;
import com.TpGrupalSpring.TpGrupal.DTO.Cliente.ClienteEdit;

import java.util.List;

public interface ClienteService {
    List<ClienteDTO> findAll();
    ClienteDTO findById(Long id);
    ClienteDTO save(ClienteCreate clienteCreate);
    ClienteDTO update(Long id, ClienteEdit clienteEdit);
    void deleteById(Long id);
}
