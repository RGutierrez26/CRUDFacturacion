package com.TpGrupalSpring.TpGrupal.Service.Cliente;

import com.TpGrupalSpring.TpGrupal.DTO.Cliente.ClienteCreate;
import com.TpGrupalSpring.TpGrupal.DTO.Cliente.ClienteDTO;
import com.TpGrupalSpring.TpGrupal.DTO.Cliente.ClienteEdit;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Cliente;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;
import com.TpGrupalSpring.TpGrupal.Repository.ClienteRepository;
import com.TpGrupalSpring.TpGrupal.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ClienteServiceimpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ClienteServiceimpl(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> findAll() {
        return clienteRepository.findByFechaBajaIsNull().stream()
                .map(ClienteDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente con ID " + id + " no encontrado"));
        return ClienteDTO.toDTO(cliente);
    }

    @Override
    public ClienteDTO save(ClienteCreate clienteCreate) {
        Usuario usuarioCarga = null;

        usuarioCarga = usuarioRepository.findById(clienteCreate.usuarioCargaId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cliente cliente = clienteCreate.ToEntity(usuarioCarga);
        cliente = clienteRepository.save(cliente);
        return ClienteDTO.toDTO(cliente);
    }

    @Override
    public ClienteDTO update(Long id, ClienteEdit clienteEdit) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente con ID " + id + " no encontrado"));

        Usuario usuarioMod = null;

            usuarioMod = usuarioRepository.findById(clienteEdit.usuarioModificacionId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        clienteEdit.applyTo(cliente, usuarioMod);
        cliente = clienteRepository.save(cliente);
        return ClienteDTO.toDTO(cliente);
    }

    @Override
    public void deleteById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente con ID " + id + " no encontrado"));
        cliente.setFechaBaja(new Date());
        clienteRepository.save(cliente);
    }
}
