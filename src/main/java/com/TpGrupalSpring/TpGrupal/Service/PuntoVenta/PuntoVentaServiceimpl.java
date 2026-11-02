package com.TpGrupalSpring.TpGrupal.Service.PuntoVenta;

import com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta.PuntoVentaCreate;
import com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta.PuntoVentaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.PuntoVenta.PuntoVentaEdit;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.PuntoVenta;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;
import com.TpGrupalSpring.TpGrupal.Repository.PuntoVentaRepository;
import com.TpGrupalSpring.TpGrupal.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PuntoVentaServiceimpl implements PuntoVentaService {

    private final PuntoVentaRepository puntoVentaRepository;
    private final UsuarioRepository usuarioRepository;

    public PuntoVentaServiceimpl(PuntoVentaRepository puntoVentaRepository, UsuarioRepository usuarioRepository) {
        this.puntoVentaRepository = puntoVentaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoVentaDTO> findAll() {
        return puntoVentaRepository.findByFechaBajaIsNull().stream()
                .map(PuntoVentaDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PuntoVentaDTO findById(Long id) {
        PuntoVenta pv = puntoVentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punto de Venta con ID " + id + " no encontrado"));
        return PuntoVentaDTO.toDTO(pv);
    }

    @Override
    public PuntoVentaDTO save(PuntoVentaCreate puntoVentaCreate) {
        Usuario usuarioCarga = null;
        if (puntoVentaCreate.usuarioCargaId() != null) {
            usuarioCarga = usuarioRepository.findById(puntoVentaCreate.usuarioCargaId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        } else {
            usuarioCarga = usuarioRepository.findAll().stream().findFirst().orElse(null);
        }

        PuntoVenta pv = puntoVentaCreate.ToEntity(usuarioCarga);
        pv = puntoVentaRepository.save(pv);
        return PuntoVentaDTO.toDTO(pv);
    }

    @Override
    public PuntoVentaDTO update(Long id, PuntoVentaEdit puntoVentaEdit) {
        PuntoVenta pv = puntoVentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punto de Venta con ID " + id + " no encontrado"));

        Usuario usuarioMod = null;
        if (puntoVentaEdit.usuarioModificacionId() != null) {
            usuarioMod = usuarioRepository.findById(puntoVentaEdit.usuarioModificacionId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        puntoVentaEdit.applyTo(pv, usuarioMod);
        pv = puntoVentaRepository.save(pv);
        return PuntoVentaDTO.toDTO(pv);
    }

    @Override
    public void deleteById(Long id) {
        PuntoVenta pv = puntoVentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punto de Venta con ID " + id + " no encontrado"));
        pv.setFechaBaja(new Date());
        puntoVentaRepository.save(pv);
    }
}
