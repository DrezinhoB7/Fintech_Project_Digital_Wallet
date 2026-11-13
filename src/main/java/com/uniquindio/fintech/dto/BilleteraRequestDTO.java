package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.TipoBilletera;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BilleteraRequestDTO(
        @NotBlank(message = "El ID del usuario propietario es obligatorio.")
        String usuarioId,

        @NotBlank(message = "El nombre de la billetera es obligatorio.")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres.")
        String nombre,

        @NotNull(message = "El tipo de billetera es obligatorio.")
        TipoBilletera tipo
) {
}