package com.uniquindio.fintech.controller;

import com.uniquindio.fintech.dto.RecargaRetiroRequestDTO;
import com.uniquindio.fintech.dto.TransaccionResponseDTO;
import com.uniquindio.fintech.dto.TransferenciaRequestDTO;
import com.uniquindio.fintech.service.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = "*")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping("/recarga")
    public ResponseEntity<TransaccionResponseDTO> recargar(@Valid @RequestBody RecargaRetiroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transaccionService.recargar(dto));
    }

    @PostMapping("/retiro")
    public ResponseEntity<TransaccionResponseDTO> retirar(@Valid @RequestBody RecargaRetiroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transaccionService.retirar(dto));
    }

    @PostMapping("/transferencia")
    public ResponseEntity<TransaccionResponseDTO> transferir(@Valid @RequestBody TransferenciaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transaccionService.transferir(dto));
    }

    @GetMapping("/historial/billetera/{codigoBilletera}")
    public ResponseEntity<List<TransaccionResponseDTO>> obtenerHistorialPorBilletera(@PathVariable String codigoBilletera) {
        return ResponseEntity.ok(transaccionService.obtenerHistorialPorBilletera(codigoBilletera));
    }

    @GetMapping("/historial/usuario/{usuarioId}")
    public ResponseEntity<List<TransaccionResponseDTO>> obtenerHistorialPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(transaccionService.obtenerHistorialPorUsuario(usuarioId));
    }

    @PostMapping("/revertir-ultima")
    public ResponseEntity<TransaccionResponseDTO> revertirUltima() {
        return ResponseEntity.ok(transaccionService.revertirUltimaTransaccion());
    }
}