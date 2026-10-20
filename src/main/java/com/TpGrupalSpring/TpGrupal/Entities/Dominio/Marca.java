package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "marca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Marca extends AuditoriaApp {
    @Column(nullable = false)
    private String denominacion;
    @Column(nullable = false)
    private Integer codigo;

    public Marca(String denominacion, Integer codigo, Usuario usuarioCarga) {
        super(usuarioCarga);
        this.denominacion = denominacion;
        this.codigo = codigo;
    }
}
