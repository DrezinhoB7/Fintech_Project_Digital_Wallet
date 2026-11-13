package com.uniquindio.fintech.dto;

public record BeneficioResponseDTO(
        String id,
        String nombre,
        String descripcion,
        int puntosRequeridos,
        boolean activo
) {
}
