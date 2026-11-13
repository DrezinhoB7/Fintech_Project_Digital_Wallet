package com.uniquindio.fintech.structures.hash;

import com.uniquindio.fintech.structures.lista.Lista;
import java.util.Objects;

public class TablaHash<K, V> {
    private static final int CAPACIDAD_DEFECTO = 31;
    private Lista<Entrada<K, V>>[] tabla;
    private int capacidad;
    private int tamano;

    public TablaHash() {
        this(CAPACIDAD_DEFECTO);
    }

    @SuppressWarnings("unchecked")
    public TablaHash(int capacidad) {
        this.capacidad = Math.max(7, capacidad);
        this.tabla = (Lista<Entrada<K, V>>[]) new Lista[this.capacidad];
        this.tamano = 0;
        for (int i = 0; i < this.capacidad; i++) {
            this.tabla[i] = new Lista<>();
        }
    }

    private int funcionHash(K clave) {
        if (clave == null) {
            return 0;
        }
        return (clave.hashCode() & 0x7fffffff) % capacidad;
    }

    public void insertar(K clave, V valor) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula en TablaHash.");
        }
        int indice = funcionHash(clave);
        Lista<Entrada<K, V>> bucket = tabla[indice];

        for (Entrada<K, V> entrada : bucket) {
            if (Objects.equals(entrada.getClave(), clave)) {
                entrada.setValor(valor);
                return;
            }
        }
        bucket.agregar(new Entrada<>(clave, valor));
        tamano++;
    }

    public V obtener(K clave) {
        if (clave == null) {
            return null;
        }
        int indice = funcionHash(clave);
        Lista<Entrada<K, V>> bucket = tabla[indice];

        for (Entrada<K, V> entrada : bucket) {
            if (Objects.equals(entrada.getClave(), clave)) {
                return entrada.getValor();
            }
        }
        return null;
    }

    public boolean eliminar(K clave) {
        if (clave == null) {
            return false;
        }
        int indice = funcionHash(clave);
        Lista<Entrada<K, V>> bucket = tabla[indice];

        for (Entrada<K, V> entrada : bucket) {
            if (Objects.equals(entrada.getClave(), clave)) {
                boolean eliminado = bucket.eliminar(entrada);
                if (eliminado) {
                    tamano--;
                }
                return eliminado;
            }
        }
        return false;
    }

    public boolean contieneClave(K clave) {
        return obtener(clave) != null;
    }

    public Lista<K> claves() {
        Lista<K> listaClaves = new Lista<>();
        for (int i = 0; i < capacidad; i++) {
            for (Entrada<K, V> entrada : tabla[i]) {
                listaClaves.agregar(entrada.getClave());
            }
        }
        return listaClaves;
    }

    public Lista<V> valores() {
        Lista<V> listaValores = new Lista<>();
        for (int i = 0; i < capacidad; i++) {
            for (Entrada<K, V> entrada : tabla[i]) {
                listaValores.agregar(entrada.getValor());
            }
        }
        return listaValores;
    }

    public int getTamano() {
        return tamano;
    }

    public void limpiar() {
        for (int i = 0; i < capacidad; i++) {
            tabla[i].limpiar();
        }
        this.tamano = 0;
    }
}