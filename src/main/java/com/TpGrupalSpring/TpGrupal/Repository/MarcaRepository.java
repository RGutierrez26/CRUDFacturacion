package com.TpGrupalSpring.TpGrupal.Repository;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Marca;
import org.springframework.stereotype.Repository;

@Repository
public interface MarcaRepository extends AudotoriaRepository<Marca, Long> {
}
