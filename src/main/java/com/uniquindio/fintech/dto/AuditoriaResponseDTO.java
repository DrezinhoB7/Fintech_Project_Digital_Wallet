package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.NivelRiesgo;
import com.uniquindio.fintech.model.Transaccion;
import com.uniquindio.fintech.model.Usuario;

import java.time.LocalDateTime;

public record AuditoriaResponseDTO(
        String id,
        LocalDateTime fecha,
        String reglaDisparada,
        String detalle,
        NivelRiesgo nivelRiesgo,
        Transaccion transaccion,
        Usuario usuario
) {
}
