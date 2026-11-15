package com.uniquindio.fintech.controller;

import com.uniquindio.fintech.dto.AristaTransferenciaDTO;
import com.uniquindio.fintech.dto.RegistroAuditoriaResponseDTO;
import com.uniquindio.fintech.dto.ResumenRedDTO;
import com.uniquindio.fintech.enums.NivelRiesgo;
import com.uniquindio.fintech.service.AnaliticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analitica")
@CrossOrigin(origins = "*")
public class AnaliticaController {

    private final AnaliticaService analiticaService;

    public AnaliticaController(AnaliticaService analiticaService) {
        this.analiticaService = analiticaService;
    }

    @GetMapping("/red/resumen")
    public ResponseEntity<ResumenRedDTO> obtenerResumenRed(
            @RequestParam(defaultValue = "1") int minFrecuencia) {
        return ResponseEntity.ok(analiticaService.obtenerResumenAnaliticoRed(minFrecuencia));
    }

    @GetMapping("/red/conexiones")
    public ResponseEntity<List<AristaTransferenciaDTO>> obtenerTodasLasConexiones() {
        return ResponseEntity.ok(analiticaService.obtenerTodasLasConexiones());
    }

    @GetMapping("/red/billetera/{codigoBilletera}")
    public ResponseEntity<List<AristaTransferenciaDTO>> obtenerConexionesBilletera(
            @PathVariable String codigoBilletera) {
        return ResponseEntity.ok(analiticaService.obtenerConexionesDesdeBilletera(codigoBilletera));
    }

    @GetMapping("/red/rutas-frecuentes")
    public ResponseEntity<List<AristaTransferenciaDTO>> obtenerRutasFrecuentes(
            @RequestParam(defaultValue = "2") int minFrecuencia) {
        return ResponseEntity.ok(analiticaService.obtenerRutasFrecuentes(minFrecuencia));
    }

    @GetMapping("/red/ciclos")
    public ResponseEntity detectarCiclos() {
        return ResponseEntity.ok(analiticaService.verificarCiclosEnRed());
    }

    @GetMapping("/auditoria")
    public ResponseEntity<List<RegistroAuditoriaResponseDTO>> listarAuditoria() {
        return ResponseEntity.ok(analiticaService.listarHistorialAuditoria());
    }

    @GetMapping("/auditoria/riesgo/{nivel}")
    public ResponseEntity<List<RegistroAuditoriaResponseDTO>> listarAuditoriaPorRiesgo(
            @PathVariable NivelRiesgo nivel) {
        return ResponseEntity.ok(analiticaService.listarAuditoriaPorRiesgo(nivel));
    }

    @GetMapping("/auditoria/usuario/{usuarioId}")
    public ResponseEntity<List<RegistroAuditoriaResponseDTO>> listarAuditoriaPorUsuario(
            @PathVariable String usuarioId) {
        return ResponseEntity.ok(analiticaService.listarAuditoriaPorUsuario(usuarioId));
    }
}