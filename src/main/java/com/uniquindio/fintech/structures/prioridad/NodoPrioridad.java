package com.uniquindio.fintech.structures.prioridad;

public class NodoPrioridad<T> {
    private T dato;
    private long prioridad;
    private NodoPrioridad<T> siguiente;

    public NodoPrioridad(T dato, long prioridad) {
        this.dato = dato;
        this.prioridad = prioridad;
        this.siguiente = null;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public long getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(long prioridad) {
        this.prioridad = prioridad;
    }

    public NodoPrioridad<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoPrioridad<T> siguiente) {
        this.siguiente = siguiente;
    }
}