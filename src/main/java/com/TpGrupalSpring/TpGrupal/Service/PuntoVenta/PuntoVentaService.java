package com.TpGrupalSpring.TpGrupal.Service.PuntoVenta;

import com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta.PuntoVentaCreate;
import com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta.PuntoVentaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta.PuntoVentaEdit;

import java.util.List;

public interface PuntoVentaService {
    List<PuntoVentaDTO> findAll();
    PuntoVentaDTO findById(Long id);
    PuntoVentaDTO save(PuntoVentaCreate puntoVentaCreate);
    PuntoVentaDTO update(Long id, PuntoVentaEdit puntoVentaEdit);
    void deleteById(Long id);
}
