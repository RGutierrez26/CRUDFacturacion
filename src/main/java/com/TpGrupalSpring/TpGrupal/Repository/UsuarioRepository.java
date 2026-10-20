package com.TpGrupalSpring.TpGrupal.Repository;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
