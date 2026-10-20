package com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecioArticulo;

public record ListaPrecioArticuloDTO(
        Long id,
        Long listaPrecioId,
        String listaPrecioDenominacion,
        Long articuloId,
        String articuloCodigo,
        String articuloDenominacion,
        double precioVenta
) {
    public static ListaPrecioArticuloDTO toDTO(ListaPrecioArticulo lpa) {
        if (lpa == null) return null;
        Long lpId = lpa.getListaPrecio() != null ? lpa.getListaPrecio().getId() : null;
        String lpNom = lpa.getListaPrecio() != null ? lpa.getListaPrecio().getDenominacion() : null;
        Long artId = lpa.getArticulo() != null ? lpa.getArticulo().getId() : null;
        String artCod = lpa.getArticulo() != null ? lpa.getArticulo().getCodigo() : null;
        String artNom = lpa.getArticulo() != null ? lpa.getArticulo().getDenominacion() : null;
        return new ListaPrecioArticuloDTO(
                lpa.getId(),
                lpId,
                lpNom,
                artId,
                artCod,
                artNom,
                lpa.getPrecioVenta()
        );
    }
}
