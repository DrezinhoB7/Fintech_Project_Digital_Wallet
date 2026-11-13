package com.uniquindio.fintech.structures.prioridad;


import com.uniquindio.fintech.structures.lista.Lista;

public class ColaPrioridad<T> {
    private NodoPrioridad<T> frente;
    private int tamano;

    public ColaPrioridad() {
        this.frente = null;
        this.tamano = 0;
    }

    public int compararPrioridad(long p1, long p2) {
        return Long.compare(p1, p2);
    }

    public void encolar(T dato, long prioridad) {
        NodoPrioridad<T> nuevo = new NodoPrioridad<>(dato, prioridad);
        if (estaVacia() || compararPrioridad(prioridad, frente.getPrioridad()) < 0) {
            nuevo.setSiguiente(frente);
            frente = nuevo;
        } else {
            NodoPrioridad<T> actual = frente;
            while (actual.getSiguiente() != null
                    && compararPrioridad(prioridad, actual.getSiguiente().getPrioridad()) >= 0) {
                actual = actual.getSiguiente();
            }
            nuevo.setSiguiente(actual.getSiguiente());
            actual.setSiguiente(nuevo);
        }
        tamano++;
    }

    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        tamano--;
        return dato;
    }

    public T verFrente() {
        return estaVacia() ? null : frente.getDato();
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamano() {
        return tamano;
    }

    public void limpiar() {
        this.frente = null;
        this.tamano = 0;
    }

    public Lista<T> recorrer() {
        Lista<T> elementos = new Lista<>();
        NodoPrioridad<T> actual = frente;
        while (actual != null) {
            elementos.agregar(actual.getDato());
            actual = actual.getSiguiente();
        }
        return elementos;
    }
}