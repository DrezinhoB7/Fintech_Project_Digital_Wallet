package com.uniquindio.fintech.enums;

public enum NivelUsuario {
    BRONCE(0, 500),
    PLATA(501, 1000),
    ORO(1001, 5000),
    PLATINO(5001, Integer.MAX_VALUE);

    private final int puntosMinimos;
    private final int puntosMaximos;

    NivelUsuario(int puntosMinimos, int puntosMaximos) {
        this.puntosMinimos = puntosMinimos;
        this.puntosMaximos = puntosMaximos;
    }

    public int getPuntosMinimos() {
        return puntosMinimos;
    }

    public int getPuntosMaximos() {
        return puntosMaximos;
    }

    public static NivelUsuario obtenerPorPuntos(int puntos) {
        if (puntos <= 500) {
            return BRONCE;
        } else if (puntos <= 1000) {
            return PLATA;
        } else if (puntos <= 5000) {
            return ORO;
        } else {
            return PLATINO;
        }
    }
}