package com.uniquindio.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransferenciaRequestDTO(
        @NotBlank(message = "El código de la billetera origen es obligatorio.")
        String codigoOrigen,

        @NotBlank(message = "El código de la billetera destino es obligatorio.")
        String codigoDestino,

        @Positive(message = "El valor a transferir debe ser mayor a cero.")
        double valor
) {
}