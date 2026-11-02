package com.TpGrupalSpring.TpGrupal.Service.ListaPrecio;

import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecio.ListaPrecioDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecio;
import com.TpGrupalSpring.TpGrupal.Repository.ListaPrecioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ListaPrecioServiceimpl implements ListaPrecioService {

    private final ListaPrecioRepository listaPrecioRepository;

    public ListaPrecioServiceimpl(ListaPrecioRepository listaPrecioRepository) {
        this.listaPrecioRepository = listaPrecioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListaPrecioDTO> findAll() {
        return listaPrecioRepository.findByFechaBajaIsNull().stream()
                .map(ListaPrecioDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ListaPrecioDTO findById(Long id) {
        ListaPrecio lp = listaPrecioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lista de Precio con ID " + id + " no encontrada"));
        return ListaPrecioDTO.toDTO(lp);
    }
}
