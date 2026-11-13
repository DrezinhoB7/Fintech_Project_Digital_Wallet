package com.uniquindio.fintech.structures.grafo;

import java.util.Objects;

public class Arista<V> {
    private V destino;
    private double pesoMonto;
    private int frecuencia;

    public Arista(V destino, double pesoMonto, int frecuencia) {
        this.destino = destino;
        this.pesoMonto = pesoMonto;
        this.frecuencia = frecuencia;
    }

    public void registrarMovimiento(double monto) {
        this.pesoMonto += monto;
        this.frecuencia++;
    }

    public V getDestino() {
        return destino;
    }

    public void setDestino(V destino) {
        this.destino = destino;
    }

    public double getPesoMonto() {
        return pesoMonto;
    }

    public void setPesoMonto(double pesoMonto) {
        this.pesoMonto = pesoMonto;
    }

    public int getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(int frecuencia) {
        this.frecuencia = frecuencia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Arista<?> arista)) return false;
        return Objects.equals(destino, arista.destino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destino);
    }
}