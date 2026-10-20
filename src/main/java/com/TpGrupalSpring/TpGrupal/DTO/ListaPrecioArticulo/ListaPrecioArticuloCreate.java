package com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Articulo;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecio;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecioArticulo;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

public record ListaPrecioArticuloCreate(
        Long listaPrecioId,
        Long articuloId,
        double precioVenta,
        Long usuarioCargaId
) {
    public ListaPrecioArticulo ToEntity(ListaPrecio listaPrecio, Articulo articulo, Usuario usuarioCarga) {
        return new ListaPrecioArticulo(
                listaPrecio,
                this.precioVenta,
                articulo,
                usuarioCarga
        );
    }
}
