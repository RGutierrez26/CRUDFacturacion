package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaEditDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaRequestDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaVentaDTO;
import com.TpGrupalSpring.TpGrupal.Service.Afip.AfipWsaaService;
import com.TpGrupalSpring.TpGrupal.Service.Afip.AfipWsfeService;
import com.TpGrupalSpring.TpGrupal.Service.FacturaVenta.FacturaVentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/facturas")
@RestController
public class FacturaVentaController {
    private final FacturaVentaService facturaVentaService;
    private final AfipWsfeService afipWsfeService;
    private final AfipWsaaService afipWsaaService;

    public FacturaVentaController(FacturaVentaService facturaVentaService, AfipWsfeService afipWsfeService, AfipWsaaService afipWsaaService) {
        this.facturaVentaService = facturaVentaService;
        this.afipWsfeService = afipWsfeService;
        this.afipWsaaService = afipWsaaService;
    }

    @GetMapping
    public ResponseEntity<List<FacturaVentaDTO>> getAll() {
        return ResponseEntity.ok(facturaVentaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaVentaDTO> getById(@PathVariable long id) {
        return ResponseEntity.ok(facturaVentaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FacturaVentaDTO> create(@RequestBody FacturaRequestDTO facturaVentaDTO) {
        FacturaVentaDTO facturaCreada = facturaVentaService.save(facturaVentaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaCreada); // 201 Created
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FacturaVentaDTO> update(@PathVariable long id, @RequestBody FacturaEditDTO facturaVentaDTO) {
        FacturaVentaDTO facturaActualizada = facturaVentaService.update(id, facturaVentaDTO);
        return ResponseEntity.ok(facturaActualizada); // 200 OK
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        facturaVentaService.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    // 2. Endpoint para ANULAR FACTURA (Regla de negocio)
    @PostMapping("/{id}/anular")
    public ResponseEntity<FacturaVentaDTO> anularFacturaPorId(@PathVariable Long id) {
        FacturaVentaDTO anulada = facturaVentaService.anularFacturaPorId(id);
        return ResponseEntity.ok(anulada);
    }



    // 3. Endpoint para FACTURAR CON ARCA / AFIP (Obtención de CAE y Número Oficial)
    @PostMapping("/{id}/facturar")
    public ResponseEntity<FacturaVentaDTO> facturarConAfip(@PathVariable Long id) {
        FacturaVentaDTO facturada = afipWsfeService.facturar(id);
        return ResponseEntity.ok(facturada);
    }

    // 4. Endpoint de diagnóstico para probar autenticación WSAA en vivo
    @GetMapping("/afip/test-wsaa")
    public ResponseEntity<String> testWsaa() {
        try {
            String[] auth = afipWsaaService.obtenerTokenYSign("wsfe");
            return ResponseEntity.ok("¡Conexión y Autenticación con AFIP WSAA Exitosa!\nToken: " + auth[0].substring(0, 40) + "...\nSign: " + auth[1].substring(0, 40) + "...");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error AFIP WSAA: " + e.getMessage());
        }
    }

}