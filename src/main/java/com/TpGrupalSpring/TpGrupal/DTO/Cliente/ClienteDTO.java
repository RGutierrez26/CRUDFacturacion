package com.TpGrupalSpring.TpGrupal.DTO.Cliente;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Cliente;

public record ClienteDTO(
        Long id,
        String cuitCuil,
        String denominacion,
        String email,
        String telefono,
        String celular,
        String domicilio
) {
    public static ClienteDTO toDTO(Cliente cliente) {
        if (cliente == null) return null;
        String email = cliente.getContacto() != null ? cliente.getContacto().getEmail() : null;
        String telefono = cliente.getContacto() != null ? cliente.getContacto().getTelefono() : null;
        String celular = cliente.getContacto() != null ? cliente.getContacto().getCelular() : null;
        String dom = null;
        if (cliente.getDomicilio() != null) {
            dom = (cliente.getDomicilio().getNombreCalle() != null ? cliente.getDomicilio().getNombreCalle() : "") + " " +
                  (cliente.getDomicilio().getNumeroCalle() != null ? cliente.getDomicilio().getNumeroCalle() : "");
            dom = dom.trim();
        }
        return new ClienteDTO(
                cliente.getId(),
                cliente.getCuitCuil(),
                cliente.getDenominacion(),
                email,
                telefono,
                celular,
                dom
        );
    }
}
