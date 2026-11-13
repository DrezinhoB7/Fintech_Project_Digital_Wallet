package com.uniquindio.fintech.structures.hash;

import java.util.Objects;

public class Entrada<K, V> {
    private K clave;
    private V valor;

    public Entrada(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public K getClave() {
        return clave;
    }

    public void setClave(K clave) {
        this.clave = clave;
    }

    public V getValor() {
        return valor;
    }

    public void setValor(V valor) {
        this.valor = valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrada<?, ?> entrada)) return false;
        return Objects.equals(clave, entrada.clave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clave);
    }
}