package com.uniquindio.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RecargaRetiroRequestDTO(
        @NotBlank(message = "El código de la billetera es obligatorio.")
        String codigoBilletera,

        @Positive(message = "El valor debe ser mayor a cero.")
        double valor
) {
}