package com.TpGrupalSpring.TpGrupal.Entities.Dominio;

import com.TpGrupalSpring.TpGrupal.Entities.Abstractas.AuditoriaApp;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Cliente extends AuditoriaApp {
    @Column(nullable = false)
    private String cuitCuil;

    @Column(nullable = false)
    private String denominacion;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contacto_id", nullable = false)
    private Contacto contacto;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "domicilio_id", nullable = false)
    private Domicilio domicilio;

    public Cliente(Usuario usuario) {
        super(usuario);
    }

    public Cliente(String cuitCuil, String denominacion, Contacto contacto, Domicilio domicilio, Usuario usuarioCarga) {
        super(usuarioCarga);
        this.cuitCuil = cuitCuil;
        this.denominacion = denominacion;
        this.contacto = contacto;
        this.domicilio = domicilio;
    }
}
