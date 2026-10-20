package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "puntoventa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PuntoVenta extends AuditoriaApp {
    @Column(nullable = false)
    private int numero;
    @Column(nullable = false)
    private String descripcion;
    @Column(nullable = false)
    private String tipoEmision;
    @Column(nullable = false)
    private String domicilioComercial;

    public PuntoVenta(int numero, String descripcion, String tipoEmision, String domicilioComercial, Usuario usuario) {
        super(usuario);
        this.numero = numero;
        this.descripcion = descripcion;
        this.tipoEmision = tipoEmision;
        this.domicilioComercial = domicilioComercial;
    }
}
