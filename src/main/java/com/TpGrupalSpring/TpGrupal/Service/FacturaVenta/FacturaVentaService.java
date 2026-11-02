package com.TpGrupalSpring.TpGrupal.Service.FacturaVenta;

import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaEditDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaRequestDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaVentaDTO;

import java.util.List;

public interface FacturaVentaService {
    List<FacturaVentaDTO> findAll();
    FacturaVentaDTO findById(Long id);
    FacturaVentaDTO save(FacturaRequestDTO requestDTO);
    FacturaVentaDTO update(Long id, FacturaEditDTO editDTO);
    void deleteById(Long id);
    FacturaVentaDTO findByNumeroFactura(Long numeroFactura);
    void deleteByNumeroFactura(Long numeroFactura);
    void darBajaLogica(Long numeroFactura); //este da baja logica
    FacturaVentaDTO anularFactura(Long numeroFactura); //este da de negocio
    FacturaVentaDTO anularFacturaPorId(Long id);
}
