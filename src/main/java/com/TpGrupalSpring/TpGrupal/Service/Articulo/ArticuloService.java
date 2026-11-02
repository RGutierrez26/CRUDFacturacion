package com.TpGrupalSpring.TpGrupal.Service.Articulo;

import com.TpGrupalSpring.TpGrupal.DTO.Articulo.ArticuloDTO;

import java.util.List;

public interface ArticuloService {
    List<ArticuloDTO> findAll();
    ArticuloDTO findById(Long id);
}
