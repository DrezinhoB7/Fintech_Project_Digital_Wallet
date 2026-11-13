package com.uniquindio.fintech.controller;

import com.uniquindio.fintech.dto.BilleteraRequestDTO;
import com.uniquindio.fintech.dto.BilleteraResponseDTO;
import com.uniquindio.fintech.enums.EstadoBilletera;
import com.uniquindio.fintech.service.BilleteraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billeteras")
@CrossOrigin(origins = "*")
public class BilleteraController {

    private final BilleteraService billeteraService;

    public BilleteraController(BilleteraService billeteraService) {
        this.billeteraService = billeteraService;
    }

    @PostMapping
    public ResponseEntity<BilleteraResponseDTO> crearBilletera(@Valid @RequestBody BilleteraRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billeteraService.crearBilletera(dto));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<BilleteraResponseDTO> consultarBilletera(@PathVariable String codigo) {
        return ResponseEntity.ok(billeteraService.consultarPorCodigo(codigo));
    }

    @PatchMapping("/{codigo}/estado")
    public ResponseEntity<BilleteraResponseDTO> cambiarEstado(@PathVariable String codigo,
                                                              @RequestParam EstadoBilletera estado) {
        return ResponseEntity.ok(billeteraService.cambiarEstado(codigo, estado));
    }
}