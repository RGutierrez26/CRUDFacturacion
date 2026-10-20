package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "facturaventa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FacturaVenta extends AuditoriaApp {
    private Long numero;

    @Column(nullable = false)
    private Date fechaEmision;

    @ManyToOne
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoVenta puntoVenta;

    private double importeCobrado;
    private double importeSaldo;

    @Column(nullable = false)
    private double importeTotal;

    private String cae;
    private Date caeFechaVencimiento;
    private String resultadoAfip;
    private String motivoRechazo;

    @Column(nullable = false)
    private String estado;

    private Date fechaAnulacion;
    private String observaciones;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<FacturaVentaDetalle> detalles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "condicion_iva_id", nullable = false)
    private CondicionIva condicionIva;

    @ManyToOne
    @JoinColumn(name = "tipo_moneda_id", nullable = false)
    private TipoMoneda tipoMoneda;

    public FacturaVenta(Usuario usuarioCarga, Long numero, Date fechaEmision, PuntoVenta puntoVenta, double importeCobrado, double importeSaldo, double importeTotal, String cae, Date caeFechaVencimiento, String motivoRechazo, String resultadoAfip, String estado, String observaciones, List<FacturaVentaDetalle> detalles, Date fechaAnulacion, Cliente cliente, CondicionIva condicionIva, TipoMoneda tipoMoneda) {
        super(usuarioCarga);
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.puntoVenta = puntoVenta;
        this.importeCobrado = importeCobrado;
        this.importeSaldo = importeSaldo;
        this.importeTotal = importeTotal;
        this.cae = cae;
        this.caeFechaVencimiento = caeFechaVencimiento;
        this.motivoRechazo = motivoRechazo;
        this.resultadoAfip = resultadoAfip;
        this.estado = estado;
        this.observaciones = observaciones;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
        this.fechaAnulacion = fechaAnulacion;
        this.cliente = cliente;
        this.condicionIva = condicionIva;
        this.tipoMoneda = tipoMoneda;
        if (this.detalles != null) {
            this.detalles.forEach(detalle -> detalle.setFactura(this));
        }
    }

    public void agregarDetalle(FacturaVentaDetalle detalle) {
        if (detalles == null) {
            detalles = new ArrayList<>();
        }
        detalles.add(detalle);
        detalle.setFactura(this);
    }
}
