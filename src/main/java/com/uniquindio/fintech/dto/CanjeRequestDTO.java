package com.uniquindio.fintech.dto;

import jakarta.validation.constraints.NotBlank;

public record CanjeRequestDTO(
        @NotBlank(message = "El ID del usuario es obligatorio.")
        String usuarioId,

        @NotBlank(message = "El ID del beneficio es obligatorio.")
        String beneficioId
) {
}