package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.TipoAlerta;
import com.uniquindio.fintech.model.Usuario;

import java.time.LocalDateTime;

public record NotificacionResponseDTO(
        String id,
        TipoAlerta tipoAlerta,
        String mensaje,
        LocalDateTime fechaDeCreacion,
        boolean leida,
        Usuario usuario
) {
}
