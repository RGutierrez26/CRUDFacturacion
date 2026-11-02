package com.TpGrupalSpring.TpGrupal.DTO.Articulo;

import com.TpGrupalSpring.TpGrupal.DTO.Marca.MarcaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.Rubro.RubroDTO;

public record ArticuloDTO(
        Long id,
        String codigo,
        String denominacion,
        MarcaDTO marcaDTO,
        RubroDTO rubroDTO
) {
    public static ArticuloDTO toDTO(com.TpGrupalSpring.TpGrupal.Entities.Dominio.Articulo articulo) {
        if (articulo == null) return null;
        return new ArticuloDTO(
                articulo.getId(),
                articulo.getCodigo(),
                articulo.getDenominacion(),
                articulo.getMarca() != null ? MarcaDTO.toDTO(articulo.getMarca()) : null,
                articulo.getRubro() != null ? RubroDTO.toDTO(articulo.getRubro()) : null
        );
    }
}
