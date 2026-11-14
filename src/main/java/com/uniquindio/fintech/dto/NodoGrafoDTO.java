package com.uniquindio.fintech.dto;

public record NodoGrafoDTO(
        String codigoBilletera,
        String nombreBilletera,
        String titular,
        int conexionesSalientes,
        int conexionesEntrantes,
        double montoTotalEnviado,
        double montoTotalRecibido
) {
}