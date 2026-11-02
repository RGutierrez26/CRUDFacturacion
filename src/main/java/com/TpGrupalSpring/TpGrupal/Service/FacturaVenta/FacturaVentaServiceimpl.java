package com.TpGrupalSpring.TpGrupal.Service.FacturaVenta;

import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaDetalleRequestDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaEditDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaRequestDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaVentaDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.*;
import com.TpGrupalSpring.TpGrupal.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class FacturaVentaServiceimpl implements FacturaVentaService {

    private final FacturaVentaRepository facturaVentaRepository;
    private final ClienteRepository clienteRepository;
    private final PuntoVentaRepository puntoVentaRepository;
    private final CondicionIvaRepository condicionIvaRepository;
    private final TipoMonedaRepository tipoMonedaRepository;
    private final ListaPrecioArticuloRepository listaPrecioArticuloRepository;
    private final UsuarioRepository usuarioRepository;

    public FacturaVentaServiceimpl(
            FacturaVentaRepository facturaVentaRepository,
            ClienteRepository clienteRepository,
            PuntoVentaRepository puntoVentaRepository,
            CondicionIvaRepository condicionIvaRepository,
            TipoMonedaRepository tipoMonedaRepository,
            ListaPrecioArticuloRepository listaPrecioArticuloRepository,
            UsuarioRepository usuarioRepository) {
        this.facturaVentaRepository = facturaVentaRepository;
        this.clienteRepository = clienteRepository;
        this.puntoVentaRepository = puntoVentaRepository;
        this.condicionIvaRepository = condicionIvaRepository;
        this.tipoMonedaRepository = tipoMonedaRepository;
        this.listaPrecioArticuloRepository = listaPrecioArticuloRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaVentaDTO> findAll() {
        return facturaVentaRepository.findByFechaBajaIsNull().stream()
                .map(FacturaVentaDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaVentaDTO findById(Long id) {
        FacturaVenta factura = facturaVentaRepository.findByIdActiva(id)
                .orElseThrow(() -> new RuntimeException("Factura con ID " + id + " no encontrada"));
        return FacturaVentaDTO.toDTO(factura);
    }
    @Override
    @Transactional(readOnly = true)
    public FacturaVentaDTO findByNumeroFactura(Long numeroFactura) {
        FacturaVenta factura = facturaVentaRepository.findByNumeroAndFechaBajaIsNull(numeroFactura)
                .orElseThrow(() -> new RuntimeException("Factura con número " + numeroFactura + " no encontrada"));
        return FacturaVentaDTO.toDTO(factura);
    }

    @Override
    public FacturaVentaDTO save(FacturaRequestDTO requestDTO) {
        if (requestDTO.detalles() == null || requestDTO.detalles().isEmpty()) {
            throw new RuntimeException("La factura debe contener al menos un detalle. No se permite crear una factura sin ítems.");
        }

        PuntoVenta puntoVenta = puntoVentaRepository.findById(requestDTO.puntoVentaId())
                .orElseThrow(() -> new RuntimeException("Punto de Venta con ID " + requestDTO.puntoVentaId() + " no encontrado"));

        CondicionIva condicionIva = condicionIvaRepository.findById(requestDTO.condicionIvaId())
                .orElseThrow(() -> new RuntimeException("Condición IVA con ID " + requestDTO.condicionIvaId() + " no encontrada"));

        TipoMoneda tipoMoneda = tipoMonedaRepository.findById(requestDTO.tipoMonedaId())
                .orElseThrow(() -> new RuntimeException("Tipo de Moneda con ID " + requestDTO.tipoMonedaId() + " no encontrado"));

        Cliente cliente = null;
        if (requestDTO.clienteId() != null) {
            cliente = clienteRepository.findById(requestDTO.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente con ID " + requestDTO.clienteId() + " no encontrado"));
        }

        Usuario usuarioCarga = null;
        if (requestDTO.usuarioCargaId() != null) {
            usuarioCarga = usuarioRepository.findById(requestDTO.usuarioCargaId()).orElse(null);
        }
        if (usuarioCarga == null) {
            usuarioCarga = usuarioRepository.findAll().stream().findFirst().orElse(null);
        }

        FacturaVenta factura = new FacturaVenta();
        factura.setUsuarioCarga(usuarioCarga);
        factura.setUsuarioModificacion(usuarioCarga);
        factura.setFechaEmision(requestDTO.fechaEmision() != null ? requestDTO.fechaEmision() : new Date());
        factura.setPuntoVenta(puntoVenta);
        factura.setCondicionIva(condicionIva);
        factura.setTipoMoneda(tipoMoneda);
        factura.setCliente(cliente);
        factura.setEstado("PENDIENTE");
        factura.setObservaciones(requestDTO.observaciones());
        factura.setDetalles(new ArrayList<>());

        double totalFactura = 0.0;

        if (requestDTO.detalles() != null) {
            for (FacturaDetalleRequestDTO detDTO : requestDTO.detalles()) {
                FacturaVentaDetalle detalle = crearDetalle(factura, detDTO);
                factura.agregarDetalle(detalle);
                totalFactura += detalle.getImporteSubtotal();
            }
        }

        totalFactura = Math.round(totalFactura * 100.0) / 100.0;
        factura.setImporteTotal(totalFactura);
        factura.setImporteSaldo(totalFactura);
        factura.setImporteCobrado(0.0);

        factura = facturaVentaRepository.save(factura);
        return FacturaVentaDTO.toDTO(factura);
    }

    @Override
    public FacturaVentaDTO update(Long id, FacturaEditDTO editDTO) {
        FacturaVenta factura = facturaVentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura con ID " + id + " no encontrada"));

        if (factura.getFechaBaja() != null) {
            throw new RuntimeException("No se puede modificar una factura que ha sido dada de baja lógica.");
        }

        if ("ANULADA".equalsIgnoreCase(factura.getEstado())) {
            throw new RuntimeException("No se puede modificar una factura que se encuentra en estado ANULADA.");
        }

        if ("EMITIDA".equalsIgnoreCase(factura.getEstado()) || (factura.getNumero() != null && factura.getNumero() > 0)) {
            throw new RuntimeException("No se puede modificar la factura ya que ha sido emitida ante AFIP.");
        }

        PuntoVenta pv = null;
        if (editDTO.puntoVentaId() != null) {
            pv = puntoVentaRepository.findById(editDTO.puntoVentaId())
                    .orElseThrow(() -> new RuntimeException("Punto de Venta con ID " + editDTO.puntoVentaId() + " no encontrado"));
        }

        CondicionIva ci = null;
        if (editDTO.condicionIvaId() != null) {
            ci = condicionIvaRepository.findById(editDTO.condicionIvaId())
                    .orElseThrow(() -> new RuntimeException("Condición IVA con ID " + editDTO.condicionIvaId() + " no encontrada"));
        }

        TipoMoneda tm = null;
        if (editDTO.tipoMonedaId() != null) {
            tm = tipoMonedaRepository.findById(editDTO.tipoMonedaId())
                    .orElseThrow(() -> new RuntimeException("Tipo de Moneda con ID " + editDTO.tipoMonedaId() + " no encontrado"));
        }

        Cliente cl = null;
        if (editDTO.clienteId() != null) {
            cl = clienteRepository.findById(editDTO.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente con ID " + editDTO.clienteId() + " no encontrado"));
        }

        Usuario usuarioMod = null;
        if (editDTO.usuarioModificacionId() != null) {
            usuarioMod = usuarioRepository.findById(editDTO.usuarioModificacionId()).orElse(null);
        }

        List<FacturaVentaDetalle> nuevosDetalles = null;
        if (editDTO.detalles() != null) {
            nuevosDetalles = new ArrayList<>();
            for (FacturaDetalleRequestDTO detDTO : editDTO.detalles()) {
                nuevosDetalles.add(crearDetalle(factura, detDTO));
            }
        }

        editDTO.applyTo(factura, pv, cl, ci, tm, usuarioMod, nuevosDetalles);

        factura = facturaVentaRepository.save(factura);
        return FacturaVentaDTO.toDTO(factura);
    }

    @Override
    public void deleteById(Long id) { //baja logica
        FacturaVenta factura = facturaVentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura con ID " + id + " no encontrada"));

        if ("EMITIDA".equalsIgnoreCase(factura.getEstado()) || (factura.getNumero() != null && factura.getNumero() > 0)) {
            throw new RuntimeException("No se puede dar de baja lógica una factura que ya ha sido emitida y autorizada legalmente ante AFIP.");
        }

        factura.setFechaBaja(new Date());
        facturaVentaRepository.save(factura);
    }

    @Override
    public void deleteByNumeroFactura(Long numeroFactura) { // da baja logica
        FacturaVenta factura = facturaVentaRepository.findByNumeroAndFechaBajaIsNull(numeroFactura)
                .orElseThrow(() -> new RuntimeException("Factura con número " + numeroFactura + " no encontrada"));
        factura.setFechaBaja(new Date());
        facturaVentaRepository.save(factura);
    }

    @Override
    public void darBajaLogica(Long numeroFactura) {
        FacturaVenta factura = facturaVentaRepository.findByNumeroAndFechaBajaIsNull(numeroFactura)
                .orElseThrow(() -> new RuntimeException("Factura con número " + numeroFactura + " no encontrada"));
        if(factura.getNumero() != null && factura.getNumero() > 0) {
            throw new RuntimeException("No se puede dar de baja la factura #" + factura.getNumero() + " ya que ha sido emitida.");
        }
        factura.setFechaBaja(new Date());
        facturaVentaRepository.save(factura);
    }

    @Override
    public FacturaVentaDTO anularFactura(Long numeroFactura) {
        FacturaVenta factura = facturaVentaRepository.findByNumeroAndFechaBajaIsNull(numeroFactura)
                .orElseThrow(() -> new RuntimeException("Factura con número " + numeroFactura + " no encontrada"));

        if (factura.getNumero() != null && factura.getNumero() > 0) {
            throw new RuntimeException("No se puede anular la factura #" + factura.getNumero() + " ya que ha sido emitida.");
        }
        if ("ANULADA".equalsIgnoreCase(factura.getEstado())) {
            throw new RuntimeException("La factura #" + numeroFactura + " ya se encuentra anulada");
        }
        factura.setFechaAnulacion(new Date());
        factura.setEstado("ANULADA");
        factura.setFechaModificacion(new Date());
        factura= facturaVentaRepository.save(factura);
        return FacturaVentaDTO.toDTO(factura);
    }

    @Override
    public FacturaVentaDTO anularFacturaPorId(Long id) {
        FacturaVenta factura = facturaVentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura con ID " + id + " no encontrada"));

        if (factura.getFechaBaja() != null) {
            throw new RuntimeException("No se puede anular una factura que ha sido dada de baja lógica.");
        }

        if ("EMITIDA".equalsIgnoreCase(factura.getEstado()) || (factura.getNumero() != null && factura.getNumero() > 0)) {
            throw new RuntimeException("No se puede anular la factura ya que ha sido emitida y autorizada legalmente ante AFIP.");
        }
        if ("ANULADA".equalsIgnoreCase(factura.getEstado())) {
            throw new RuntimeException("La factura ya se encuentra anulada");
        }
        factura.setFechaAnulacion(new Date());
        factura.setEstado("ANULADA");
        factura.setFechaModificacion(new Date());
        factura = facturaVentaRepository.save(factura);
        return FacturaVentaDTO.toDTO(factura);
    }


    private FacturaVentaDetalle crearDetalle(FacturaVenta factura, FacturaDetalleRequestDTO detDTO) {
        if (detDTO.cantidad() <= 0) {
            throw new RuntimeException("La cantidad del ítem debe ser mayor a cero.");
        }
        if (detDTO.porcentajeBonificacion() < 0 || detDTO.porcentajeBonificacion() > 100) {
            throw new RuntimeException("El porcentaje de bonificación debe estar comprendido entre 0% y 100%.");
        }

        ListaPrecioArticulo lpa = listaPrecioArticuloRepository.findById(detDTO.listaPrecioArticuloId())
                .orElseThrow(() -> new RuntimeException("Precio de Artículo con ID " + detDTO.listaPrecioArticuloId() + " no encontrado"));

        if (lpa.getFechaBaja() != null) {
            throw new RuntimeException("El artículo seleccionado se encuentra dado de baja lógica.");
        }

        double precioUnitario = detDTO.precioUnitario() > 0 ? detDTO.precioUnitario() : lpa.getPrecioVenta();
        double bonificacion = detDTO.porcentajeBonificacion();
        double importeBruto = Math.round(detDTO.cantidad() * precioUnitario * 100.0) / 100.0;
        double descuento = Math.round(importeBruto * (bonificacion / 100.0) * 100.0) / 100.0;
        double importeNeto = Math.round((importeBruto - descuento) * 100.0) / 100.0;
        double importeIva = Math.round(importeNeto * 0.21 * 100.0) / 100.0;
        double importeSubtotal = Math.round((importeNeto + importeIva) * 100.0) / 100.0;

        String descripcion = (detDTO.descripcion() != null && !detDTO.descripcion().isBlank())
                ? detDTO.descripcion()
                : (lpa.getArticulo() != null ? lpa.getArticulo().getDenominacion() : "");

        return new FacturaVentaDetalle(
                lpa,
                factura,
                descripcion,
                detDTO.cantidad(),
                precioUnitario,
                bonificacion,
                importeNeto,
                importeIva,
                importeSubtotal
        );
    }
}
