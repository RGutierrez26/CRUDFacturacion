package com.TpGrupalSpring.TpGrupal.Service.TipoMoneda;

import com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda.TipoMonedaCreate;
import com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda.TipoMonedaDTO;
import com.TpGrupalSpring.TpGrupal.DTO.TipoMoneda.TipoMonedaEdit;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.TipoMoneda;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;
import com.TpGrupalSpring.TpGrupal.Repository.TipoMonedaRepository;
import com.TpGrupalSpring.TpGrupal.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TipoMonedaServiceimpl implements TipoMonedaService {

    private final TipoMonedaRepository tipoMonedaRepository;
    private final UsuarioRepository usuarioRepository;

    public TipoMonedaServiceimpl(TipoMonedaRepository tipoMonedaRepository, UsuarioRepository usuarioRepository) {
        this.tipoMonedaRepository = tipoMonedaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMonedaDTO> findAll() {
        return tipoMonedaRepository.findAll().stream()
                .map(TipoMonedaDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoMonedaDTO findById(Long id) {
        TipoMoneda tm = tipoMonedaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de Moneda con ID " + id + " no encontrado"));
        return TipoMonedaDTO.toDTO(tm);
    }

    @Override
    public TipoMonedaDTO save(TipoMonedaCreate tipoMonedaCreate) {
        Usuario usuarioCarga = null;
        if (tipoMonedaCreate.usuarioCargaId() != null) {
            usuarioCarga = usuarioRepository.findById(tipoMonedaCreate.usuarioCargaId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        } else {
            usuarioCarga = usuarioRepository.findAll().stream().findFirst().orElse(null);
        }

        TipoMoneda tm = tipoMonedaCreate.ToEntity(usuarioCarga);
        tm = tipoMonedaRepository.save(tm);
        return TipoMonedaDTO.toDTO(tm);
    }

    @Override
    public TipoMonedaDTO update(Long id, TipoMonedaEdit tipoMonedaEdit) {
        TipoMoneda tm = tipoMonedaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de Moneda con ID " + id + " no encontrado"));

        Usuario usuarioMod = null;
        if (tipoMonedaEdit.usuarioModificacionId() != null) {
            usuarioMod = usuarioRepository.findById(tipoMonedaEdit.usuarioModificacionId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        tipoMonedaEdit.applyTo(tm, usuarioMod);
        tm = tipoMonedaRepository.save(tm);
        return TipoMonedaDTO.toDTO(tm);
    }

    @Override
    public void deleteById(Long id) {
        TipoMoneda tm = tipoMonedaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de Moneda con ID " + id + " no encontrado"));
        tm.setFechaBaja(new Date());
        tipoMonedaRepository.save(tm);
    }
}
