package com.TpGrupalSpring.TpGrupal.Service.ListaPrecioArticulo;

import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloCreate;
import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloDTO;
import com.TpGrupalSpring.TpGrupal.DTO.ListaPrecioArticulo.ListaPrecioArticuloEdit;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Articulo;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecio;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecioArticulo;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;
import com.TpGrupalSpring.TpGrupal.Repository.ArticuloRepository;
import com.TpGrupalSpring.TpGrupal.Repository.ListaPrecioArticuloRepository;
import com.TpGrupalSpring.TpGrupal.Repository.ListaPrecioRepository;
import com.TpGrupalSpring.TpGrupal.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ListaPrecioArticuloServiceimpl implements ListaPrecioArticuloService {

    private final ListaPrecioArticuloRepository listaPrecioArticuloRepository;
    private final ListaPrecioRepository listaPrecioRepository;
    private final ArticuloRepository articuloRepository;
    private final UsuarioRepository usuarioRepository;

    public ListaPrecioArticuloServiceimpl(
            ListaPrecioArticuloRepository listaPrecioArticuloRepository,
            ListaPrecioRepository listaPrecioRepository,
            ArticuloRepository articuloRepository,
            UsuarioRepository usuarioRepository) {
        this.listaPrecioArticuloRepository = listaPrecioArticuloRepository;
        this.listaPrecioRepository = listaPrecioRepository;
        this.articuloRepository = articuloRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListaPrecioArticuloDTO> findAll() {
        return listaPrecioArticuloRepository.findAll().stream()
                .map(ListaPrecioArticuloDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ListaPrecioArticuloDTO findById(Long id) {
        ListaPrecioArticulo lpa = listaPrecioArticuloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Precio de Artículo con ID " + id + " no encontrado"));
        return ListaPrecioArticuloDTO.toDTO(lpa);
    }

    @Override
    public ListaPrecioArticuloDTO save(ListaPrecioArticuloCreate createDTO) {
        ListaPrecio listaPrecio = listaPrecioRepository.findById(createDTO.listaPrecioId())
                .orElseThrow(() -> new RuntimeException("Lista de Precio con ID " + createDTO.listaPrecioId() + " no encontrada"));

        Articulo articulo = articuloRepository.findById(createDTO.articuloId())
                .orElseThrow(() -> new RuntimeException("Artículo con ID " + createDTO.articuloId() + " no encontrado"));

        Usuario usuarioCarga = null;

        usuarioCarga = usuarioRepository.findById(createDTO.usuarioCargaId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ListaPrecioArticulo lpa = createDTO.ToEntity(listaPrecio, articulo, usuarioCarga);
        lpa = listaPrecioArticuloRepository.save(lpa);
        return ListaPrecioArticuloDTO.toDTO(lpa);
    }

    @Override
    public ListaPrecioArticuloDTO update(Long id, ListaPrecioArticuloEdit editDTO) {
        ListaPrecioArticulo lpa = listaPrecioArticuloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Precio de Artículo con ID " + id + " no encontrado"));

        ListaPrecio listaPrecio = null;

            listaPrecio = listaPrecioRepository.findById(editDTO.listaPrecioId())
                    .orElseThrow(() -> new RuntimeException("Lista de Precio con ID " + editDTO.listaPrecioId() + " no encontrada"));


        Articulo articulo = null;

            articulo = articuloRepository.findById(editDTO.articuloId())
                    .orElseThrow(() -> new RuntimeException("Artículo con ID " + editDTO.articuloId() + " no encontrado"));


        Usuario usuarioMod = null;

            usuarioMod = usuarioRepository.findById(editDTO.usuarioModificacionId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        editDTO.applyTo(lpa, listaPrecio, articulo, usuarioMod);
        lpa = listaPrecioArticuloRepository.save(lpa);
        return ListaPrecioArticuloDTO.toDTO(lpa);
    }

    @Override
    public void deleteById(Long id) {
        ListaPrecioArticulo lpa = listaPrecioArticuloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Precio de Artículo con ID " + id + " no encontrado"));
        lpa.setFechaBaja(new Date());
        listaPrecioArticuloRepository.save(lpa);
    }
}
