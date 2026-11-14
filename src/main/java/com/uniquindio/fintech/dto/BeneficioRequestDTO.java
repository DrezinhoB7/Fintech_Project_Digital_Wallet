package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.NivelUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BeneficioRequestDTO(
        @NotBlank(message = "El nombre del beneficio es obligatorio.")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres.")
        String nombre,

        @NotBlank(message = "La descripción del beneficio es obligatoria.")
        @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres.")
        String descripcion,

        @Positive(message = "El costo en puntos debe ser mayor a cero.")
        int costoPuntos,

        @NotNull(message = "El nivel requerido es obligatorio.")
        NivelUsuario nivelRequerido
) {
}