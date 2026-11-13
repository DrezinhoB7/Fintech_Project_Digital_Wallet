package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.EstadoBilletera;
import com.uniquindio.fintech.enums.TipoBilletera;

public record BilleteraResponseDTO(
        String codigo,
        String nombre,
        TipoBilletera tipo,
        double saldoDisponible,
        EstadoBilletera estado,
        String usuarioId,
        String usuarioNombre
) {
}