package com.TpGrupalSpring.TpGrupal.Service.Marca;

import com.TpGrupalSpring.TpGrupal.DTO.Marca.MarcaDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Marca;
import com.TpGrupalSpring.TpGrupal.Repository.MarcaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MarcaServiceimpl implements MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaServiceimpl(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaDTO> findAll() {
        return marcaRepository.findAll().stream()
                .map(MarcaDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaDTO findById(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca con ID " + id + " no encontrada"));
        return MarcaDTO.toDTO(marca);
    }
}
