package com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo;

import com.TpGrupalSpring.TpGrupal.DTO.Articulo.ArticuloDTO;
import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecio.ListaPrecioDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecioArticulo;

public record ListaPrecioArticuloDTO(
        Long id,
        Double precioVenta,
        ListaPrecioDTO listaPrecioDTO,
        ArticuloDTO articuloDTO
) {
    public static ListaPrecioArticuloDTO toDTO(ListaPrecioArticulo listaPrecioArticulo) {
        if (listaPrecioArticulo == null) return null;
        return new ListaPrecioArticuloDTO(
                listaPrecioArticulo.getId(),
                listaPrecioArticulo.getPrecioVenta(),
                ListaPrecioDTO.toDTO(listaPrecioArticulo.getListaPrecio()),
                ArticuloDTO.toDTO(listaPrecioArticulo.getArticulo())
        );
    }
}
