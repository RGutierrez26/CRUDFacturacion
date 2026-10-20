package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.EntityId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facturaventadetalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FacturaVentaDetalle extends EntityId {
    @ManyToOne
    @JoinColumn(name = "factura_venta_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FacturaVenta factura;

    @ManyToOne
    @JoinColumn(name = "lista_precio_articulo_id", nullable = false)
    private ListaPrecioArticulo listaPrecioArticulo;

    private String descripcion;

    @Column(nullable = false)
    private double cantidad;

    @Column(nullable = false)
    private double precioUnitario;

    private double porcentajeBonificacion;

    private double importeNeto;

    private double importeIva;

    @Column(nullable = false)
    private double importeSubtotal;

    public FacturaVentaDetalle(ListaPrecioArticulo listaPrecioArticulo, FacturaVenta factura, String descripcion, double cantidad, double precioUnitario, double porcentajeBonificacion, double importeNeto, double importeIva, double importeSubtotal) {
        super();
        this.listaPrecioArticulo = listaPrecioArticulo;
        this.factura = factura;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.porcentajeBonificacion = porcentajeBonificacion;
        this.importeNeto = importeNeto;
        this.importeIva = importeIva;
        this.importeSubtotal = importeSubtotal;
    }
}
