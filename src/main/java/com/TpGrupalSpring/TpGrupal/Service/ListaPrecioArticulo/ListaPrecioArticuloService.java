package com.TpGrupalSpring.TpGrupal.Service.ListaPrecioArticulo;

import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloCreate;
import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloDTO;
import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloEdit;

import java.util.List;

public interface ListaPrecioArticuloService {
    List<ListaPrecioArticuloDTO> findAll();
    ListaPrecioArticuloDTO findById(Long id);
    ListaPrecioArticuloDTO save(ListaPrecioArticuloCreate createDTO);
    ListaPrecioArticuloDTO update(Long id, ListaPrecioArticuloEdit editDTO);
    void deleteById(Long id);
    List<ListaPrecioArticuloDTO> findByListaPrecioId(Long listaPrecioId);
}
