package com.TpGrupalSpring.TpGrupal.Repository;

import com.TpGrupalSpring.TpGrupal.Entities.Dominio.FacturaVenta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaVentaRepository extends AudotoriaRepository<FacturaVenta, Long> {

    // Factura por número (sin filtrar baja lógica)
    @Query("SELECT f FROM FacturaVenta f WHERE f.numero = :numeroFactura")
    Optional<FacturaVenta> findByNumero(@Param("numeroFactura") Long numeroFactura);

    // Factura por número activa (sin baja lógica)
    Optional<FacturaVenta> findByNumeroAndFechaBajaIsNull(Long numero);
    List<FacturaVenta> findByFechaBajaIsNull();

    // Factura por ID activa (sin baja lógica)
    @Query("SELECT f FROM FacturaVenta f WHERE f.id = :id AND f.fechaBaja IS NULL")
    Optional<FacturaVenta> findByIdActiva(@Param("id") Long id);

}