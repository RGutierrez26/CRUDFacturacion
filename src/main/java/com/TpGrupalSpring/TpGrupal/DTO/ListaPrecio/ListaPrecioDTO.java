package com.TpGrupalSpring.TpGrupal.DTO.ListaPrecio;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecio;

public record ListaPrecioDTO(
        Long id,
        String codigo,
        String denominacion
) {
    public static ListaPrecioDTO toDTO(ListaPrecio listaPrecio) {
        if(listaPrecio == null) return null;
        return new ListaPrecioDTO(
                listaPrecio.getId(),
                listaPrecio.getCodigo(),
                listaPrecio.getDenominacion()
        );
    };
}
