package com.TpGrupalSpring.TpGrupal.Service.Rubro;

import com.TpGrupalSpring.TpGrupal.DTO.Rubro.RubroDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Rubro;
import com.TpGrupalSpring.TpGrupal.Repository.RubroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RubroServiceimpl implements RubroService {

    private final RubroRepository rubroRepository;

    public RubroServiceimpl(RubroRepository rubroRepository) {
        this.rubroRepository = rubroRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RubroDTO> findAll() {
        return rubroRepository.findAll().stream()
                .map(RubroDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RubroDTO findById(Long id) {
        Rubro rubro = rubroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rubro con ID " + id + " no encontrado"));
        return RubroDTO.toDTO(rubro);
    }
}
