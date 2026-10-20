package com.TpGrupalSpring.TpGrupal.Service.TipoMoneda;

import com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda.TipoMonedaCreate;
import com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda.TipoMonedaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda.TipoMonedaEdit;

import java.util.List;

public interface TipoMonedaService {
    List<TipoMonedaDTO> findAll();
    TipoMonedaDTO findById(Long id);
    TipoMonedaDTO save(TipoMonedaCreate tipoMonedaCreate);
    TipoMonedaDTO update(Long id, TipoMonedaEdit tipoMonedaEdit);
    void deleteById(Long id);
}
