package com.uniquindio.fintech.structures.pila;

import com.uniquindio.fintech.structures.lista.Lista;
import com.uniquindio.fintech.structures.lista.Nodo;

public class Pila<T> {
    private Nodo<T> tope;
    private int tamano;

    public Pila() {
        this.tope = null;
        this.tamano = 0;
    }

    public void apilar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.setSiguiente(tope);
        tope = nuevo;
        tamano++;
    }

    public T desapilar() {
        if (estaVacia()) {
            return null;
        }
        T dato = tope.getDato();
        tope = tope.getSiguiente();
        tamano--;
        return dato;
    }

    public T verTope() {
        return estaVacia() ? null : tope.getDato();
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int getTamano() {
        return tamano;
    }

    public void limpiar() {
        this.tope = null;
        this.tamano = 0;
    }

    public Lista<T> recorrer() {
        Lista<T> elementos = new Lista<>();
        Nodo<T> actual = tope;
        while (actual != null) {
            elementos.agregar(actual.getDato());
            actual = actual.getSiguiente();
        }
        return elementos;
    }
}