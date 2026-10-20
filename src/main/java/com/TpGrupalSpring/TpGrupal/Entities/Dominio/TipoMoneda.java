package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "tipomoneda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TipoMoneda extends AuditoriaApp {
    @Column(nullable = false)
    private String codigoAfip;
    @Column(nullable = false)
    private String denominacion;
    @Column(nullable = false)
    private String simbolo;

    public TipoMoneda(String codigoAfip, String denominacion, String simbolo, Usuario usuarioCarga) {
        super(usuarioCarga);
        this.codigoAfip = codigoAfip;
        this.denominacion = denominacion;
        this.simbolo = simbolo;
    }
}
