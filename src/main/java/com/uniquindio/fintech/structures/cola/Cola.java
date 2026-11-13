package com.uniquindio.fintech.structures.cola;

import com.uniquindio.fintech.structures.lista.Lista;
import com.uniquindio.fintech.structures.lista.Nodo;

public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamano;

    public Cola() {
        this.frente = null;
        this.fin = null;
        this.tamano = 0;
    }

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.setSiguiente(nuevo);
            fin = nuevo;
        }
        tamano++;
    }

    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) {
            fin = null;
        }
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
        this.fin = null;
        this.tamano = 0;
    }

    public Lista<T> recorrer() {
        Lista<T> elementos = new Lista<>();
        Nodo<T> actual = frente;
        while (actual != null) {
            elementos.agregar(actual.getDato());
            actual = actual.getSiguiente();
        }
        return elementos;
    }
}