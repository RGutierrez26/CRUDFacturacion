package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "articulo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Articulo extends AuditoriaApp {
    @ManyToOne
    @JoinColumn(name = "rubro_id", nullable = false)
    private Rubro rubro;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String denominacion;

    @ManyToOne
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    public Articulo(Usuario usuarioCarga, Rubro rubro, String codigo, String denominacion, Marca marca) {
        super(usuarioCarga);
        this.rubro = rubro;
        this.codigo = codigo;
        this.denominacion = denominacion;
        this.marca = marca;
    }
}
