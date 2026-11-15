package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.NivelRiesgo;

import java.time.LocalDateTime;

public record RegistroAuditoriaResponseDTO(
        String id,
        LocalDateTime fechaDeteccion,
        String reglaActivada,
        String descripcion,
        NivelRiesgo nivelRiesgo,
        String transaccionId,
        double valorTransaccion,
        String usuarioId,
        String nombreUsuario
) {
}
