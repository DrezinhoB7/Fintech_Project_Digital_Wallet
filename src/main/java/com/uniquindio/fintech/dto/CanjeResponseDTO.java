package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.NivelUsuario;

import java.time.LocalDateTime;

public record CanjeResponseDTO(
        Long id,
        String usuarioId,
        String nombreUsuario,
        String beneficioId,
        String nombreBeneficio,
        int puntosDescontados,
        int puntosRestantes,
        NivelUsuario nivelActualUsuario,
        LocalDateTime fechaCanje
) {
}