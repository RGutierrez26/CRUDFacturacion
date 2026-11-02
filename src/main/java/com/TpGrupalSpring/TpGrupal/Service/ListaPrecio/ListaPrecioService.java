package com.TpGrupalSpring.TpGrupal.Service.ListaPrecio;

import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecio.ListaPrecioDTO;

import java.util.List;

public interface ListaPrecioService {
    List<ListaPrecioDTO> findAll();
    ListaPrecioDTO findById(Long id);
}
