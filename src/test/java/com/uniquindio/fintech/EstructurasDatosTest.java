package com.uniquindio.fintech;

import com.uniquindio.fintech.structures.arbol.ArbolBST;
import com.uniquindio.fintech.structures.cola.Cola;
import com.uniquindio.fintech.structures.grafo.Grafo;
import com.uniquindio.fintech.structures.lista.Lista;
import com.uniquindio.fintech.structures.pila.Pila;
import com.uniquindio.fintech.structures.hash.TablaHash;
import com.uniquindio.fintech.structures.prioridad.ColaPrioridad;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstructurasDatosTest {

    @Test
    void testTablaHashInsercionBusquedaYEliminacion() {
        TablaHash<String, String> tabla = new TablaHash<>();
        tabla.insertar("1094900111", "Pacho Gonzales");
        tabla.insertar("1094900222", "Andrés Gómez");

        assertTrue(tabla.contieneClave("1094900111"));
        assertEquals("Pacho Gonzales", tabla.obtener("1094900111"));

        tabla.eliminar("1094900111");
        assertFalse(tabla.contieneClave("1094900111"));
        assertNull(tabla.obtener("1094900111"));
    }

    @Test
    void testArbolBSTOrdenamientoYRango() {
        ArbolBST<Integer> arbol = new ArbolBST<>();
        arbol.insertar(500);
        arbol.insertar(120);
        arbol.insertar(850);
        arbol.insertar(300);

        List<Integer> ordenados = new ArrayList<>();
        for (Integer valor : arbol.inOrden()) {
            ordenados.add(valor);
        }
        assertEquals(List.of(120, 300, 500, 850), ordenados);

        List<Integer> rango = new ArrayList<>();
        for (Integer valor : arbol.buscarRango(200, 600)) {
            rango.add(valor);
        }
        assertEquals(List.of(300, 500), rango);

        arbol.eliminar(300);
        List<Integer> trasEliminar = new ArrayList<>();
        for (Integer valor : arbol.inOrden()) {
            trasEliminar.add(valor);
        }
        assertEquals(List.of(120, 500, 850), trasEliminar);
    }

    @Test
    void testPilaLIFO() {
        Pila<String> pila = new Pila<>();
        assertTrue(pila.estaVacia());

        pila.apilar("TX-001");
        pila.apilar("TX-002");
        pila.apilar("TX-003");

        assertFalse(pila.estaVacia());
        assertEquals("TX-003", pila.desapilar());
        assertEquals("TX-002", pila.desapilar());
    }

    @Test
    void testListaEnlazadaIteracion() {
        Lista<String> lista = new Lista<>();
        lista.agregar("RECARGA");
        lista.agregar("TRANSFERENCIA");
        lista.agregar("RETIRO");

        List<String> elementos = new ArrayList<>();
        for (String item : lista) {
            elementos.add(item);
        }
        assertEquals(3, elementos.size());
        assertEquals("TRANSFERENCIA", elementos.get(1));
    }

    @Test
    void testGrafoVerticesInicialesSinCiclos() {
        Grafo<String> grafo = new Grafo<>();
        grafo.agregarVertice("BIL-A");
        grafo.agregarVertice("BIL-B");

        assertFalse(grafo.detectarCiclos());
    }

    @Test
    void testColaFIFO() {
        Cola<String> cola = new Cola<>();
        assertTrue(cola.estaVacia());

        cola.encolar("NOTIF-001");
        cola.encolar("NOTIF-002");
        cola.encolar("NOTIF-003");

        assertFalse(cola.estaVacia());
        assertEquals("NOTIF-001", cola.desencolar());
        assertEquals("NOTIF-002", cola.desencolar());
        assertEquals("NOTIF-003", cola.desencolar());
        assertTrue(cola.estaVacia());
    }

    @Test
    void testColaPrioridad() {
        ColaPrioridad<String> colaPrioridad = new ColaPrioridad<>();
        assertTrue(colaPrioridad.estaVacia());

        colaPrioridad.encolar("Operación tardia", 3000L);
        colaPrioridad.encolar("Operación urgente", 1000L);
        colaPrioridad.encolar("Operación lejana", 5000L);
        colaPrioridad.encolar("Operación normal", 2000L);

        assertFalse(colaPrioridad.estaVacia());
        assertEquals("Operación urgente", colaPrioridad.desencolar());
        assertEquals("Operación normal", colaPrioridad.desencolar());
        assertEquals("Operación tardia", colaPrioridad.desencolar());
        assertEquals("Operación lejana", colaPrioridad.desencolar());
        assertTrue(colaPrioridad.estaVacia());
    }
}