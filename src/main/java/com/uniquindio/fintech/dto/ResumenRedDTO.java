package com.uniquindio.fintech.dto;

import java.util.List;

public record ResumenRedDTO(
        int totalNodos,
        int totalConexiones,
        boolean existenCiclosSospechosos,
        List<AristaTransferenciaDTO> rutasFrecuentes,
        List<NodoGrafoDTO> billeterasMasActivas
) {
}