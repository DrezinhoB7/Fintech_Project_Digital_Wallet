package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.NivelUsuario;

public record BeneficioResponseDTO(
        String id,
        String nombre,
        String descripcion,
        int costoPuntos,
        NivelUsuario nivelRequerido
) {
}