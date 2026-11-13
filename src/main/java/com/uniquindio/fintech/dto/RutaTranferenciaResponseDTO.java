package com.uniquindio.fintech.dto;

public record RutaTranferenciaResponseDTO(
        boolean existeRuta,
        Object salto,
        Object rutaCodigoBilletera
) {
}
