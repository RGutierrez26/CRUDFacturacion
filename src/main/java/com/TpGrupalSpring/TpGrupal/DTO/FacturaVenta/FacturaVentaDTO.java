package com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta;

import com.TpGrupalSpring.TpGrupal.DTO.Cliente.ClienteDTO;
import com.TpGrupalSpring.TpGrupal.DTO.CondicionIva.CondicionIvaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta.PuntoVentaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda.TipoMonedaDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.FacturaVenta;

import java.util.Date;
import java.util.List;

public record FacturaVentaDTO(
        Long id,
        Long numero,
        Date fechaEmision,
        PuntoVentaDTO puntoVenta,
        ClienteDTO cliente,
        CondicionIvaDTO condicionIva,
        TipoMonedaDTO tipoMoneda,
        double importeTotal,
        double importeCobrado,
        double importeSaldo,
        String estado,
        Date fechaAnulacion,
        String observaciones,
        List<FacturaVentaDetalleDTO> detalles
) {
    public static FacturaVentaDTO toDTO(FacturaVenta f) {
        if (f == null) return null;
        List<FacturaVentaDetalleDTO> dets = f.getDetalles() != null
                ? f.getDetalles().stream().map(FacturaVentaDetalleDTO::toDTO).toList()
                : List.of();
        return new FacturaVentaDTO(
                f.getId(),
                f.getNumero(),
                f.getFechaEmision(),
                PuntoVentaDTO.toDTO(f.getPuntoVenta()),
                ClienteDTO.toDTO(f.getCliente()),
                CondicionIvaDTO.toDTO(f.getCondicionIva()),
                TipoMonedaDTO.toDTO(f.getTipoMoneda()),
                f.getImporteTotal(),
                f.getImporteCobrado(),
                f.getImporteSaldo(),
                f.getEstado(),
                f.getFechaAnulacion(),
                f.getObservaciones(),
                dets
        );
    }
}
