package com.uniquindio.fintech.structures.arbol;

import com.uniquindio.fintech.structures.lista.Lista;

public class ArbolBST<T extends Comparable<T>> {
    private NodoArbol<T> raiz;

    public ArbolBST() {
        this.raiz = null;
    }

    public void insertar(T dato) {
        if (dato == null) {
            return;
        }
        raiz = insertarRec(raiz, dato);
    }

    private NodoArbol<T> insertarRec(NodoArbol<T> actual, T dato) {
        if (actual == null) {
            return new NodoArbol<>(dato);
        }
        int cmp = dato.compareTo(actual.getDato());
        if (cmp < 0) {
            actual.setIzquierdo(insertarRec(actual.getIzquierdo(), dato));
        } else if (cmp > 0) {
            actual.setDerecho(insertarRec(actual.getDerecho(), dato));
        } else {
            actual.setDato(dato);
        }
        return actual;
    }

    public void eliminar(T dato) {
        if (dato == null) {
            return;
        }
        raiz = eliminarRec(raiz, dato);
    }

    private NodoArbol<T> eliminarRec(NodoArbol<T> actual, T dato) {
        if (actual == null) {
            return null;
        }
        int cmp = dato.compareTo(actual.getDato());
        if (cmp < 0) {
            actual.setIzquierdo(eliminarRec(actual.getIzquierdo(), dato));
        } else if (cmp > 0) {
            actual.setDerecho(eliminarRec(actual.getDerecho(), dato));
        } else {
            if (actual.getIzquierdo() == null) {
                return actual.getDerecho();
            }
            if (actual.getDerecho() == null) {
                return actual.getIzquierdo();
            }
            NodoArbol<T> sucesor = encontrarMinimo(actual.getDerecho());
            actual.setDato(sucesor.getDato());
            actual.setDerecho(eliminarRec(actual.getDerecho(), sucesor.getDato()));
        }
        return actual;
    }

    public void eliminarSi(java.util.function.Predicate<T> criterio) {
        if (criterio == null) {
            return;
        }
        raiz = eliminarSiRec(raiz, criterio);
    }

    private NodoArbol<T> eliminarSiRec(NodoArbol<T> actual, java.util.function.Predicate<T> criterio) {
        if (actual == null) {
            return null;
        }
        actual.setIzquierdo(eliminarSiRec(actual.getIzquierdo(), criterio));
        actual.setDerecho(eliminarSiRec(actual.getDerecho(), criterio));

        if (criterio.test(actual.getDato())) {
            if (actual.getIzquierdo() == null) {
                return actual.getDerecho();
            }
            if (actual.getDerecho() == null) {
                return actual.getIzquierdo();
            }
            NodoArbol<T> sucesor = encontrarMinimo(actual.getDerecho());
            actual.setDato(sucesor.getDato());
            actual.setDerecho(eliminarRec(actual.getDerecho(), sucesor.getDato()));
        }
        return actual;
    }

    private NodoArbol<T> encontrarMinimo(NodoArbol<T> nodo) {
        while (nodo.getIzquierdo() != null) {
            nodo = nodo.getIzquierdo();
        }
        return nodo;
    }

    public Lista<T> buscarRango(T min, T max) {
        Lista<T> resultado = new Lista<>();
        buscarRangoRec(raiz, min, max, resultado);
        return resultado;
    }

    private void buscarRangoRec(NodoArbol<T> actual, T min, T max, Lista<T> resultado) {
        if (actual == null) {
            return;
        }
        if (min.compareTo(actual.getDato()) < 0) {
            buscarRangoRec(actual.getIzquierdo(), min, max, resultado);
        }
        if (min.compareTo(actual.getDato()) <= 0 && max.compareTo(actual.getDato()) >= 0) {
            resultado.agregar(actual.getDato());
        }
        if (max.compareTo(actual.getDato()) > 0) {
            buscarRangoRec(actual.getDerecho(), min, max, resultado);
        }
    }

    public Lista<T> inOrden() {
        Lista<T> resultado = new Lista<>();
        inOrdenRec(raiz, resultado);
        return resultado;
    }

    private void inOrdenRec(NodoArbol<T> actual, Lista<T> resultado) {
        if (actual != null) {
            inOrdenRec(actual.getIzquierdo(), resultado);
            resultado.agregar(actual.getDato());
            inOrdenRec(actual.getDerecho(), resultado);
        }
    }

    public Lista<T> inOrdenDescendente() {
        Lista<T> resultado = new Lista<>();
        inOrdenDescRec(raiz, resultado);
        return resultado;
    }

    private void inOrdenDescRec(NodoArbol<T> actual, Lista<T> resultado) {
        if (actual != null) {
            inOrdenDescRec(actual.getDerecho(), resultado);
            resultado.agregar(actual.getDato());
            inOrdenDescRec(actual.getIzquierdo(), resultado);
        }
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public void limpiar() {
        this.raiz = null;
    }
}