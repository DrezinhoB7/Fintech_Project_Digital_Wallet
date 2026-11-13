package com.uniquindio.fintech.controller;

import com.uniquindio.fintech.dto.BilleteraResponseDTO;
import com.uniquindio.fintech.dto.UsuarioRequestDTO;
import com.uniquindio.fintech.dto.UsuarioResponseDTO;
import com.uniquindio.fintech.service.BilleteraService;
import com.uniquindio.fintech.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final BilleteraService billeteraService;

    public UsuarioController(UsuarioService usuarioService, BilleteraService billeteraService) {
        this.usuarioService = usuarioService;
        this.billeteraService = billeteraService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarUsuario(dto));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/{identificacion}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorIdentificacion(@PathVariable String identificacion) {
        return ResponseEntity.ok(usuarioService.buscarPorIdentificacion(identificacion));
    }

    @GetMapping("/{usuarioId}/billeteras")
    public ResponseEntity<List<BilleteraResponseDTO>> listarBilleterasDeUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(billeteraService.listarPorUsuario(usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(@PathVariable String id,
                                                                @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}