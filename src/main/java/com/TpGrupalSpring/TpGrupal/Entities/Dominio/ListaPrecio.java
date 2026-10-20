package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "listaprecio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ListaPrecio extends AuditoriaApp {
    @Column(nullable = false)
    private String codigo;
    @Column(nullable = false)
    private String denominacion;

    public ListaPrecio(String codigo, String denominacion, Usuario usuario) {
        super(usuario);
        this.codigo = codigo;
        this.denominacion = denominacion;
    }
}