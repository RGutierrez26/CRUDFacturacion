package com.TpGrupalSpring.TpGrupal.Service.CondicionIva;

import com.TpGrupalSpring.TpGrupal.DTO.CondicionIva.CondicionIvaCreate;
import com.TpGrupalSpring.TpGrupal.DTO.CondicionIva.CondicionIvaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.CondicionIva.CondicionIvaEdit;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.CondicionIva;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;
import com.TpGrupalSpring.TpGrupal.Repository.CondicionIvaRepository;
import com.TpGrupalSpring.TpGrupal.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CondicionIvaServiceimpl implements CondicionIvaService {

    private final CondicionIvaRepository condicionIvaRepository;
    private final UsuarioRepository usuarioRepository;

    public CondicionIvaServiceimpl(CondicionIvaRepository condicionIvaRepository, UsuarioRepository usuarioRepository) {
        this.condicionIvaRepository = condicionIvaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CondicionIvaDTO> findAll() {
        return condicionIvaRepository.findByFechaBajaIsNull().stream()
                .map(CondicionIvaDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CondicionIvaDTO findById(Long id) {
        CondicionIva ci = condicionIvaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condicion IVA con ID " + id + " no encontrada"));
        return CondicionIvaDTO.toDTO(ci);
    }

    @Override
    public CondicionIvaDTO save(CondicionIvaCreate condicionIvaCreate) {
        Usuario usuarioCarga = null;
        if (condicionIvaCreate.usuarioCargaId() != null) {
            usuarioCarga = usuarioRepository.findById(condicionIvaCreate.usuarioCargaId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        } else {
            usuarioCarga = usuarioRepository.findAll().stream().findFirst().orElse(null);
        }

        CondicionIva ci = condicionIvaCreate.ToEntity(usuarioCarga);
        ci = condicionIvaRepository.save(ci);
        return CondicionIvaDTO.toDTO(ci);
    }

    @Override
    public CondicionIvaDTO update(Long id, CondicionIvaEdit condicionIvaEdit) {
        CondicionIva ci = condicionIvaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condicion IVA con ID " + id + " no encontrada"));

        Usuario usuarioMod = null;
        if (condicionIvaEdit.usuarioModificacionId() != null) {
            usuarioMod = usuarioRepository.findById(condicionIvaEdit.usuarioModificacionId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        condicionIvaEdit.applyTo(ci, usuarioMod);
        ci = condicionIvaRepository.save(ci);
        return CondicionIvaDTO.toDTO(ci);
    }

    @Override
    public void deleteById(Long id) {
        CondicionIva ci = condicionIvaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condicion IVA con ID " + id + " no encontrada"));
        ci.setFechaBaja(new Date());
        condicionIvaRepository.save(ci);
    }
}
