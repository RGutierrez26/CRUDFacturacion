package com.TpGrupalSpring.TpGrupal.Service.Rubro;

import com.TpGrupalSpring.TpGrupal.DTO.Rubro.RubroDTO;

import java.util.List;

public interface RubroService {
    List<RubroDTO> findAll();
    RubroDTO findById(Long id);
}
