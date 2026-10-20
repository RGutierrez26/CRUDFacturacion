package com.TpGrupalSpring.TpGrupal.Entities.Abstractas;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AuditoriaApp extends EntityId {
    @Temporal(TemporalType.DATE)
    @Column(nullable = false, name = "fechaalta")
    protected Date fechaAlta;

    @Temporal(TemporalType.DATE )
    @Column(nullable = true, name = "fechabaja")
    protected Date fechaBaja;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false, name = "fechamodificacion")
    protected Date fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_carga_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    protected Usuario usuarioCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_baja_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    protected Usuario usuarioBaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    protected Usuario usuarioModificacion;

    public AuditoriaApp() {
        this.fechaAlta = new Date();
        this.fechaBaja = null;
        this.fechaModificacion = new Date();
    }

    public AuditoriaApp(Usuario usuarioCarga) {
        this.fechaAlta = new Date();
        this.fechaBaja = null;
        this.fechaModificacion = new Date();
        this.usuarioCarga = usuarioCarga;
        this.usuarioBaja = null;
        this.usuarioModificacion = usuarioCarga;
    }

    public void setFechaBaja(Usuario usuarioBaja) {
        this.fechaBaja = new Date();
        this.usuarioBaja = usuarioBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public void setFechaModificacion(Usuario usuarioModificacion) {
        this.fechaModificacion = new Date();
        this.usuarioModificacion = usuarioModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public void setFechaAlta(Date fechaAlta, Usuario usuarioCarga) {
        this.fechaAlta = fechaAlta;
        this.usuarioCarga = usuarioCarga;
    }
}


