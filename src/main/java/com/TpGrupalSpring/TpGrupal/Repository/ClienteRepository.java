package com.TpGrupalSpring.TpGrupal.Repository;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.Cliente;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.events.Event;

@Repository
public interface ClienteRepository extends AudotoriaRepository<Cliente, Long> {

}
