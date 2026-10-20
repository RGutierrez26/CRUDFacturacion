package com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.*;

import java.util.Date;
import java.util.List;

public record FacturaEditDTO(
        Long puntoVentaId,
        Long clienteId,
        Long condicionIvaId,
        Long tipoMonedaId,
        Long numero,
        Date fechaEmision,
        String estado,
        String observaciones,
        Long usuarioModificacionId,
        List<FacturaDetalleRequestDTO> detalles
) {
    public void applyTo(
            FacturaVenta factura,
            PuntoVenta puntoVenta,
            Cliente cliente,
            CondicionIva condicionIva,
            TipoMoneda tipoMoneda,
            Usuario usuarioMod,
            List<FacturaVentaDetalle> nuevosDetalles
    ) {
        if (puntoVenta != null) {
            factura.setPuntoVenta(puntoVenta);
        }
        if (cliente != null) {
            factura.setCliente(cliente);
        }
        if (condicionIva != null) {
            factura.setCondicionIva(condicionIva);
        }
        if (tipoMoneda != null) {
            factura.setTipoMoneda(tipoMoneda);
        }

        if (this.numero != null) {
            factura.setNumero(this.numero);
        }
        if (this.fechaEmision != null) {
            factura.setFechaEmision(this.fechaEmision);
        }
        if (this.estado != null && !this.estado.isBlank()) {
            factura.setEstado(this.estado);
            if ("ANULADA".equalsIgnoreCase(this.estado)) {
                factura.setFechaAnulacion(new Date());
            }
        }
        if (this.observaciones != null) {
            factura.setObservaciones(this.observaciones);
        }

        if (usuarioMod != null) {
            factura.setFechaModificacion(usuarioMod);
        } else {
            factura.setFechaModificacion(new Date());
        }

        if (nuevosDetalles != null && !nuevosDetalles.isEmpty()) {
            factura.getDetalles().clear();
            double total = 0.0;
            for (FacturaVentaDetalle det : nuevosDetalles) {
                factura.agregarDetalle(det);
                total += det.getImporteSubtotal();
            }
            factura.setImporteTotal(total);
            factura.setImporteSaldo(total - factura.getImporteCobrado());
        }
    }
}
