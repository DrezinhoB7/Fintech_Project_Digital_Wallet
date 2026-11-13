package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.NivelUsuario;
import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        String id,
        String identificacion,
        String nombre,
        String email,
        String telefono,
        int puntosAcumulados,
        NivelUsuario nivel,
        boolean activo,
        LocalDateTime fechaRegistro,
        int cantidadBilleteras,
        double saldoTotal
) {
}