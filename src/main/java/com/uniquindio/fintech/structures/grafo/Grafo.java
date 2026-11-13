package com.uniquindio.fintech.structures.grafo;

import com.uniquindio.fintech.structures.hash.TablaHash;
import com.uniquindio.fintech.structures.lista.Lista;
import java.util.Objects;

public class Grafo<V> {
    private TablaHash<V, Lista<Arista<V>>> adyacencia;
    private Lista<V> vertices;

    public Grafo() {
        this.adyacencia = new TablaHash<>(53);
        this.vertices = new Lista<>();
    }

    public void agregarVertice(V v) {
        if (v != null && !adyacencia.contieneClave(v)) {
            adyacencia.insertar(v, new Lista<>());
            vertices.agregar(v);
        }
    }

    public void agregarArista(V origen, V destino) {
        agregarArista(origen, destino, 0.0);
    }

    public void agregarArista(V origen, V destino, double monto) {
        if (origen == null || destino == null) {
            return;
        }
        agregarVertice(origen);
        agregarVertice(destino);

        Lista<Arista<V>> vecinos = adyacencia.obtener(origen);
        for (Arista<V> arista : vecinos) {
            if (Objects.equals(arista.getDestino(), destino)) {
                arista.registrarMovimiento(monto);
                return;
            }
        }
        vecinos.agregar(new Arista<>(destino, monto, 1));
    }

    public Lista<Arista<V>> obtenerVecinos(V v) {
        Lista<Arista<V>> vecinos = adyacencia.obtener(v);
        return vecinos != null ? vecinos : new Lista<>();
    }

    public Lista<V> obtenerVertices() {
        return vertices;
    }

    public boolean detectarCiclos() {
        // 0 = no visitado, 1 = en pila de recursion (activo), 2 = completamente visitado
        TablaHash<V, Integer> estadoVisita = new TablaHash<>(53);
        for (V vertice : vertices) {
            estadoVisita.insertar(vertice, 0);
        }

        for (V vertice : vertices) {
            Integer estado = estadoVisita.obtener(vertice);
            if (estado != null && estado == 0) {
                if (dfsCiclo(vertice, estadoVisita)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfsCiclo(V actual, TablaHash<V, Integer> estadoVisita) {
        estadoVisita.insertar(actual, 1);

        Lista<Arista<V>> vecinos = obtenerVecinos(actual);
        for (Arista<V> arista : vecinos) {
            V destino = arista.getDestino();
            Integer estadoDestino = estadoVisita.obtener(destino);
            if (estadoDestino != null && estadoDestino == 1) {
                return true;
            }
            if (estadoDestino != null && estadoDestino == 0) {
                if (dfsCiclo(destino, estadoVisita)) {
                    return true;
                }
            }
        }

        estadoVisita.insertar(actual, 2);
        return false;
    }

    public Lista<String> obtenerRutasFrecuentes(int minFrecuencia) {
        Lista<String> rutas = new Lista<>();
        for (V origen : vertices) {
            for (Arista<V> arista : obtenerVecinos(origen)) {
                if (arista.getFrecuencia() >= minFrecuencia) {
                    rutas.agregar(origen + " -> " + arista.getDestino()
                            + " (Frecuencia: " + arista.getFrecuencia()
                            + ", Monto acumulado: $" + String.format("%.2f", arista.getPesoMonto()) + ")");
                }
            }
        }
        return rutas;
    }

    public void limpiar() {
        this.adyacencia.limpiar();
        this.vertices.limpiar();
    }
}