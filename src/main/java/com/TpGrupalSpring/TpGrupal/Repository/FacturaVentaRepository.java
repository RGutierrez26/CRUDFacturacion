package com.TpGrupalSpring.TpGrupal.Repository;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.FacturaVenta;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface FacturaVentaRepository extends AudotoriaRepository<FacturaVenta, Long> {


    Optional<FacturaVenta> findByNumero(Long numeroFactura);
}
