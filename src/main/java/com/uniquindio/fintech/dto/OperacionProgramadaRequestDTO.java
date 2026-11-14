package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.FrecuenciaOperacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record OperacionProgramadaRequestDTO(
        @NotBlank(message = "El código de la billetera de origen es obligatorio") String codigoBilleteraOrigen,
        @NotBlank(message = "El código de la billetera de destino es obligatorio") String codigoBilleteraDestino,
        @Positive(message = "El valor debe ser mayor a cero") double valor,
        @NotNull(message = "Debe ingresar una fecha de ejecución") LocalDateTime fechaEjecucion,
        @NotNull(message = "La frecuencia es obligatoria") FrecuenciaOperacion frecuencia
) {
}
