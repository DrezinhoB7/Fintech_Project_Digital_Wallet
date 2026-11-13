package com.uniquindio.fintech.dto;

import java.time.LocalDateTime;

public record CanjeResponseDTO(
        String id,
        LocalDateTime fechaCanje,
        int puntosUsados,
        String usuarioNombre,
        String BeneficioNombre,
        int puntosRestantes
) {
}
