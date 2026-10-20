package com.TpGrupalSpring.TpGrupal.Repository;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Articulo;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticuloRepository extends AudotoriaRepository<Articulo, Long> {
}
