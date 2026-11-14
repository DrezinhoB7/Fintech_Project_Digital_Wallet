package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.FrecuenciaOperacion;
import com.uniquindio.fintech.enums.TipoTransaccion;

import java.time.LocalDateTime;

public record OperacionProgramadaResponseDTO(
        String id,
        LocalDateTime fechaEjecucion,
        TipoTransaccion tipoOperacion,
        double valor,
        FrecuenciaOperacion frecuencia,
        boolean activa,
        String codigoBilleteraOrigen,
        String codigoBilleteraDestino
) {
}

