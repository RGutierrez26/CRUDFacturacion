package com.TpGrupalSpring.TpGrupal.DTO.Cliente;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Cliente;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Contacto;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Domicilio;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;

import java.util.Date;

public record ClienteEdit(
        String cuitCuil,
        String denominacion,
        String email,
        String telefono,
        String celular,
        String nombreCalle,
        String numeroCalle,
        Long usuarioModificacionId
) {
    public void applyTo(Cliente cliente, Usuario usuarioMod) {
        if (this.cuitCuil != null) {
            cliente.setCuitCuil(this.cuitCuil);
        }
        if (this.denominacion != null) {
            cliente.setDenominacion(this.denominacion);
        }

        if (cliente.getContacto() == null) {
            cliente.setContacto(new Contacto());
        }
        if (this.email != null) {
            cliente.getContacto().setEmail(this.email);
        }
        if (this.telefono != null) {
            cliente.getContacto().setTelefono(this.telefono);
        }
        if (this.celular != null) {
            cliente.getContacto().setCelular(this.celular);
        }

        if (cliente.getDomicilio() == null) {
            cliente.setDomicilio(new Domicilio());
        }
        if (this.nombreCalle != null) {
            cliente.getDomicilio().setNombreCalle(this.nombreCalle);
        }
        if (this.numeroCalle != null) {
            cliente.getDomicilio().setNumeroCalle(this.numeroCalle);
        }

        if (usuarioMod != null) {
            cliente.setFechaModificacion(usuarioMod);
        } else {
            cliente.setFechaModificacion(new Date());
        }
    }
}
