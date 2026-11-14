package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.EstadoTransaccion;
import com.uniquindio.fintech.enums.NivelRiesgo;
import com.uniquindio.fintech.enums.TipoTransaccion;

import java.time.LocalDateTime;

public record TransaccionResponseDTO(
        String id,
        LocalDateTime fecha,
        TipoTransaccion tipoTransaccion,
        double valor,
        EstadoTransaccion estado,
        int puntosGenerados,
        NivelRiesgo nivelRiesgo,
        boolean reversible,
        String codigoOrigen,
        String codigoDestino
) {
}