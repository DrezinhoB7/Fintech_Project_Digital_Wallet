package com.uniquindio.fintech.controller;

import com.uniquindio.fintech.dto.BeneficioRequestDTO;
import com.uniquindio.fintech.dto.BeneficioResponseDTO;
import com.uniquindio.fintech.dto.CanjeRequestDTO;
import com.uniquindio.fintech.dto.CanjeResponseDTO;
import com.uniquindio.fintech.dto.UsuarioResponseDTO;
import com.uniquindio.fintech.service.BeneficioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficios")
@CrossOrigin(origins = "*")
public class BeneficioController {

    private final BeneficioService beneficioService;

    public BeneficioController(BeneficioService beneficioService) {
        this.beneficioService = beneficioService;
    }

    @PostMapping
    public ResponseEntity<BeneficioResponseDTO> crearBeneficio(@Valid @RequestBody BeneficioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(beneficioService.crearBeneficio(dto));
    }

    @GetMapping
    public ResponseEntity<List<BeneficioResponseDTO>> listarBeneficios() {
        return ResponseEntity.ok(beneficioService.listarBeneficios());
    }

    @GetMapping("/disponibles/{usuarioId}")
    public ResponseEntity<List<BeneficioResponseDTO>> listarDisponibles(@PathVariable String usuarioId) {
        return ResponseEntity.ok(beneficioService.listarBeneficiosDisponiblesParaUsuario(usuarioId));
    }

    @PostMapping("/canjear")
    public ResponseEntity<CanjeResponseDTO> canjearBeneficio(@Valid @RequestBody CanjeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(beneficioService.canjearBeneficio(dto));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<BeneficioResponseDTO>> listarCanjeadosPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(beneficioService.listarBeneficiosCanjeadosPorUsuario(usuarioId));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerRanking() {
        return ResponseEntity.ok(beneficioService.obtenerRankingUsuarios());
    }
}