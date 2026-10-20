package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "listaprecioarticulo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ListaPrecioArticulo extends AuditoriaApp {
    @ManyToOne
    @JoinColumn(name = "lista_precio_id", nullable = false)
    private ListaPrecio listaPrecio;

    @Column(nullable = false)
    private double precioVenta;

    @ManyToOne
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    public ListaPrecioArticulo(ListaPrecio listaPrecio, double precioVenta, Articulo articulo, Usuario usuarioCarga) {
        super(usuarioCarga);
        this.listaPrecio = listaPrecio;
        this.precioVenta = precioVenta;
        this.articulo = articulo;
    }
}
