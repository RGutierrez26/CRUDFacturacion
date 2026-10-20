package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "condicioniva")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CondicionIva extends AuditoriaApp {
    @Column(nullable = false)
    private int codigoAfip;
    @Column(nullable = false)
    private String denominacion;

    public CondicionIva(int codigoAfip, String denominacion, Usuario usuarioCarga) {
        super(usuarioCarga);
        this.codigoAfip = codigoAfip;
        this.denominacion = denominacion;
    }
}
