package com.TpGrupalSpring.TpGrupal.Controller;

import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaEditDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaRequestDTO;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaVentaDTO;
import com.TpGrupalSpring.TpGrupal.Service.FacturaVenta.FacturaVentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/facturas")
@RestController
public class FacturaVentaController {
    private final FacturaVentaService facturaVentaService;

    public FacturaVentaController(FacturaVentaService facturaVentaService) {
        this.facturaVentaService = facturaVentaService;
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
    @GetMapping("/numero/{numero}")
    public ResponseEntity<FacturaVentaDTO> getByNumero(@PathVariable Long numero) {
        return ResponseEntity.ok(facturaVentaService.findByNumeroFactura(numero));
    }
    @DeleteMapping("/numero/{numero}")
    public ResponseEntity<Void> deleteByNumero(@PathVariable Long numero) {
        facturaVentaService.deleteByNumeroFactura(numero);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}