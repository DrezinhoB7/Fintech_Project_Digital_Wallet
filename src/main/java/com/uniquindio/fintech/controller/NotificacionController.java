package com.uniquindio.fintech.controller;

import com.uniquindio.fintech.dto.NotificacionResponseDTO;
import com.uniquindio.fintech.service.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacionResponseDTO>> obtenerHistorialPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(notificacionService.obtenerHistorialPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<List<NotificacionResponseDTO>> obtenerNoLeidasPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(notificacionService.obtenerNoLeidasPorUsuario(usuarioId));
    }

    @GetMapping("/cola")
    public ResponseEntity<List<NotificacionResponseDTO>> listarColaPendientes() {
        return ResponseEntity.ok(notificacionService.listarColaNotificacionesPendientes());
    }

    @PostMapping("/cola/procesar-siguiente")
    public ResponseEntity<NotificacionResponseDTO> procesarSiguienteNotificacion() {
        return ResponseEntity.ok(notificacionService.procesarSiguienteNotificacion());
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificacionResponseDTO> marcarComoLeida(@PathVariable String id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }

    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    public ResponseEntity<List<NotificacionResponseDTO>> marcarTodasComoLeidas(@PathVariable String usuarioId) {
        return ResponseEntity.ok(notificacionService.marcarTodasComoLeidasPorUsuario(usuarioId));
    }

    @PostMapping("/alertas/evaluar/{usuarioId}")
    public ResponseEntity<List<NotificacionResponseDTO>> evaluarAlertasAutomaticas(@PathVariable String usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.evaluarAlertasAutomaticasPorUsuario(usuarioId));
    }

    @PostMapping("/alertas/saldo-bajo/{usuarioId}")
    public ResponseEntity<NotificacionResponseDTO> verificarSaldoBajo(@PathVariable String usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.verificarYNotificarSaldoBajo(usuarioId));
    }

    @PostMapping("/alertas/operaciones-proximas")
    public ResponseEntity<List<NotificacionResponseDTO>> notificarOperacionesProximas() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.notificarOperacionesProgramadasProximas());
    }

    @PostMapping("/alertas/operacion-rechazada/{usuarioId}")
    public ResponseEntity<NotificacionResponseDTO> notificarOperacionRechazada(
            @PathVariable String usuarioId,
            @RequestParam String motivo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.notificarOperacionRechazada(usuarioId, motivo));
    }

    @PostMapping("/alertas/ascenso-nivel/{usuarioId}")
    public ResponseEntity<NotificacionResponseDTO> notificarAscensoNivel(@PathVariable String usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.notificarAscensoNivel(usuarioId));
    }

    @PostMapping("/alertas/canje-beneficio/{usuarioId}")
    public ResponseEntity<NotificacionResponseDTO> notificarCanjeBeneficio(
            @PathVariable String usuarioId,
            @RequestParam String nombreBeneficio,
            @RequestParam int puntosDescontados) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.notificarCanjeBeneficio(usuarioId, nombreBeneficio, puntosDescontados));
    }
}
