package com.uniquindio.fintech.controller;

import com.uniquindio.fintech.dto.OperacionProgramadaRequestDTO;
import com.uniquindio.fintech.dto.OperacionProgramadaResponseDTO;
import com.uniquindio.fintech.service.OperacionProgramadaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operaciones-programadas")
@CrossOrigin(origins = "*")
public class OperacionProgramadaController {

    private final OperacionProgramadaService operacionProgramadaService;

    public OperacionProgramadaController(OperacionProgramadaService operacionProgramadaService) {
        this.operacionProgramadaService = operacionProgramadaService;
    }

    @PostMapping
    public ResponseEntity<OperacionProgramadaResponseDTO> programarOperacion(@Valid @RequestBody OperacionProgramadaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operacionProgramadaService.nuevaOperacionProgramada(dto));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<OperacionProgramadaResponseDTO>> listarOperacionesPendientes() {
        return ResponseEntity.ok(operacionProgramadaService.listarOperacionesPendientes());
    }

    @PostMapping("/ejecutar")
    public ResponseEntity<List<OperacionProgramadaResponseDTO>> ejecutarOperacionesPendientes() {
        return ResponseEntity.ok(operacionProgramadaService.ejecutarOperacionesPendientes());
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OperacionProgramadaResponseDTO> cancelarOperacion(@PathVariable String id) {
        return ResponseEntity.ok(operacionProgramadaService.cancelarOperacionProgramada(id));
    }
}
