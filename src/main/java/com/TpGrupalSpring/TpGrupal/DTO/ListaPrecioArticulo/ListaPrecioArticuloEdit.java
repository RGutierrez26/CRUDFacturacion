package com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Articulo;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecio;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecioArticulo;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

import java.util.Date;

public record ListaPrecioArticuloEdit(
        Long listaPrecioId,
        Long articuloId,
        double precioVenta,
        Long usuarioModificacionId
) {
    public void applyTo(ListaPrecioArticulo lpa, ListaPrecio listaPrecio, Articulo articulo, Usuario usuarioMod) {
        if (listaPrecio != null) {
            lpa.setListaPrecio(listaPrecio);
        }
        if (articulo != null) {
            lpa.setArticulo(articulo);
        }
        if (this.precioVenta != 0) {
            lpa.setPrecioVenta(this.precioVenta);
        }
        if (usuarioMod != null) {
            lpa.setFechaModificacion(usuarioMod);
        } else {
            lpa.setFechaModificacion(new Date());
        }
    }
}
