package com.TpGrupalSpring.TpGrupal.Service.Marca;

import com.TpGrupalSpring.TpGrupal.DTO.Marca.MarcaDTO;

import java.util.List;

public interface MarcaService {
    List<MarcaDTO> findAll();
    MarcaDTO findById(Long id);
}
