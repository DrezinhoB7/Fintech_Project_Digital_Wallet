package com.uniquindio.fintech.dto;

public record AristaTransferenciaDTO(
        String codigoOrigen,
        String titularOrigen,
        String codigoDestino,
        String titularDestino,
        int frecuencia,
        double montoAcumulado
) {
}