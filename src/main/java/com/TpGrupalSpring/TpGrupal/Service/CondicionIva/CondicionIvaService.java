package com.TpGrupalSpring.TpGrupal.Service.CondicionIva;

import com.TpGrupalSpring.TpGrupal.DTO.CondicionIva.CondicionIvaCreate;
import com.TpGrupalSpring.TpGrupal.DTO.CondicionIva.CondicionIvaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.CondicionIva.CondicionIvaEdit;

import java.util.List;

public interface CondicionIvaService {
    List<CondicionIvaDTO> findAll();
    CondicionIvaDTO findById(Long id);
    CondicionIvaDTO save(CondicionIvaCreate condicionIvaCreate);
    CondicionIvaDTO update(Long id, CondicionIvaEdit condicionIvaEdit);
    void deleteById(Long id);
}
