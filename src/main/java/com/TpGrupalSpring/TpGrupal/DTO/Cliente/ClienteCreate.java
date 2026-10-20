package com.TpGrupalSpring.TpGrupal.DTO.Cliente;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Cliente;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Contacto;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Domicilio;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

public record ClienteCreate(
        String cuitCuil,
        String denominacion,
        String email,
        String telefono,
        String celular,
        String nombreCalle,
        String numeroCalle,
        Long usuarioCargaId
) {
    public Cliente ToEntity(Usuario usuarioCarga) {
        Contacto contacto = Contacto.builder()
                .email(this.email)
                .telefono(this.telefono)
                .celular(this.celular)
                .build();

        Domicilio domicilio = Domicilio.builder()
                .nombreCalle(this.nombreCalle)
                .numeroCalle(this.numeroCalle)
                .build();

        return new Cliente(
                this.cuitCuil,
                this.denominacion,
                contacto,
                domicilio,
                usuarioCarga
        );
    }
}
