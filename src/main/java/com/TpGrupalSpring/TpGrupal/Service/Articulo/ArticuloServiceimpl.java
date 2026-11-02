package com.TpGrupalSpring.TpGrupal.Service.Articulo;

import com.TpGrupalSpring.TpGrupal.DTO.Articulo.ArticuloDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Articulo;
import com.TpGrupalSpring.TpGrupal.Repository.ArticuloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArticuloServiceimpl implements ArticuloService {

    private final ArticuloRepository articuloRepository;

    public ArticuloServiceimpl(ArticuloRepository articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloDTO> findAll() {
        return articuloRepository.findAll().stream()
                .map(ArticuloDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloDTO findById(Long id) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo con ID " + id + " no encontrado"));
        return ArticuloDTO.toDTO(articulo);
    }
}
