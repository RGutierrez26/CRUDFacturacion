package com.TpGrupalSpring.TpGrupal.Repository;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.ListaPrecioArticulo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListaPrecioArticuloRepository extends AudotoriaRepository<ListaPrecioArticulo, Long> {
    List<ListaPrecioArticulo> findByListaPrecioId(Long listaPrecioId);
    List<ListaPrecioArticulo> findByListaPrecioIdAndFechaBajaIsNull(Long listaPrecioId);
}
