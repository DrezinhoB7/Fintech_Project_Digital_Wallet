package com.uniquindio.fintech.config;

import com.uniquindio.fintech.enums.EstadoTransaccion;
import com.uniquindio.fintech.enums.TipoTransaccion;
import com.uniquindio.fintech.model.*;
import com.uniquindio.fintech.repository.*;
import com.uniquindio.fintech.structures.arbol.ArbolBST;
import com.uniquindio.fintech.structures.cola.Cola;
import com.uniquindio.fintech.structures.grafo.Grafo;
import com.uniquindio.fintech.structures.hash.TablaHash;
import com.uniquindio.fintech.structures.lista.Lista;
import com.uniquindio.fintech.structures.pila.Pila;
import com.uniquindio.fintech.structures.prioridad.ColaPrioridad;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

@Component
public class PlataformaFintechContext {

    private final TablaHash<String, Usuario> usuariosPorId = new TablaHash<>(53);
    private final TablaHash<String, Billetera> billeterasPorCodigo = new TablaHash<>(101);
    private final ArbolBST<Usuario> arbolPuntos = new ArbolBST<>();
    private final ColaPrioridad<OperacionProgramada> colaProgramadas = new ColaPrioridad<>();
    private final Cola<Notificacion> colaNotificaciones = new Cola<>();
    private final Pila<Transaccion> pilaReversiones = new Pila<>();
    private final Grafo<Billetera> grafoTransferencias = new Grafo<>();

    private final TablaHash<String, Lista<Transaccion>> historialPorUsuario = new TablaHash<>(53);
    private final TablaHash<String, Lista<Transaccion>> historialPorBilletera = new TablaHash<>(101);
    private final TablaHash<String, Lista<Beneficio>> beneficiosPorUsuario = new TablaHash<>(53);
    private final Lista<RegistroAuditoria> historialAuditoria = new Lista<>();

    private final UsuarioRepository usuarioRepository;
    private final BilleteraRepository billeteraRepository;
    private final TransaccionRepository transaccionRepository;
    private final OperacionProgramadaRepository operacionProgramadaRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioBeneficioRepository usuarioBeneficioRepository;
    private final RegistroAuditoriaRepository auditoriaRepository;

    public PlataformaFintechContext(UsuarioRepository usuarioRepository,
                                    BilleteraRepository billeteraRepository,
                                    TransaccionRepository transaccionRepository,
                                    OperacionProgramadaRepository operacionProgramadaRepository,
                                    NotificacionRepository notificacionRepository,
                                    UsuarioBeneficioRepository usuarioBeneficioRepository,
                                    RegistroAuditoriaRepository auditoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.billeteraRepository = billeteraRepository;
        this.transaccionRepository = transaccionRepository;
        this.operacionProgramadaRepository = operacionProgramadaRepository;
        this.notificacionRepository = notificacionRepository;
        this.usuarioBeneficioRepository = usuarioBeneficioRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @PostConstruct
    public synchronized void recargarDesdeBaseDeDatos() {
        usuariosPorId.limpiar();
        billeterasPorCodigo.limpiar();
        arbolPuntos.limpiar();
        colaProgramadas.limpiar();
        colaNotificaciones.limpiar();
        pilaReversiones.limpiar();
        grafoTransferencias.limpiar();
        historialPorUsuario.limpiar();
        historialPorBilletera.limpiar();
        beneficiosPorUsuario.limpiar();
        historialAuditoria.limpiar();

        // 1. Cargar usuarios activos en TablaHash y ArbolBST
        List<Usuario> usuarios = usuarioRepository.findByActivoTrue();
        for (Usuario u : usuarios) {
            usuariosPorId.insertar(u.getIdentificacion(), u);
            arbolPuntos.insertar(u);
            historialPorUsuario.insertar(u.getId(), new Lista<>());
            beneficiosPorUsuario.insertar(u.getId(), new Lista<>());

            for (UsuarioBeneficio ub : usuarioBeneficioRepository.findByUsuarioIdOrderByFechaCanjeDesc(u.getId())) {
                beneficiosPorUsuario.obtener(u.getId()).agregar(ub.getBeneficio());
            }
        }

        // 2. Cargar billeteras en TablaHash y vértices del Grafo
        List<Billetera> billeteras = billeteraRepository.findAll();
        for (Billetera b : billeteras) {
            billeterasPorCodigo.insertar(b.getCodigo(), b);
            historialPorBilletera.insertar(b.getCodigo(), new Lista<>());
            grafoTransferencias.agregarVertice(b);
        }

        // 3. Cargar transacciones en Listas, Pila de reversiones y aristas del Grafo
        List<Transaccion> transacciones = transaccionRepository.findAllByOrderByFechaAsc();
        for (Transaccion tx : transacciones) {
            registrarTransaccionEnEstructuras(tx);
        }

        // 4. Cargar operaciones programadas activas en ColaPrioridad
        List<OperacionProgramada> programadas = operacionProgramadaRepository.findByActivaTrueOrderByFechaEjecucionAsc();
        for (OperacionProgramada op : programadas) {
            long prioridad = op.getFechaEjecucion().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            colaProgramadas.encolar(op, prioridad);
        }

        // 5. Cargar notificaciones no leídas en Cola FIFO
        for (Notificacion n : notificacionRepository.findAll()) {
            if (!n.isLeida()) {
                colaNotificaciones.encolar(n);
            }
        }

        // 6. Cargar registros de auditoría en Lista
        for (RegistroAuditoria reg : auditoriaRepository.findAllByOrderByFechaDeteccionDesc()) {
            historialAuditoria.agregar(reg);
        }
    }

    public void registrarTransaccionEnEstructuras(Transaccion tx) {
        Billetera origen = tx.getBilleteraOrigen();
        Billetera destino = tx.getBilleteraDestino();

        if (origen != null) {
            obtenerListaBilletera(origen.getCodigo()).agregar(tx);
            obtenerListaUsuario(origen.getUsuario().getId()).agregar(tx);
        }
        if (destino != null) {
            obtenerListaBilletera(destino.getCodigo()).agregar(tx);
            if (origen == null || !origen.getUsuario().getId().equals(destino.getUsuario().getId())) {
                obtenerListaUsuario(destino.getUsuario().getId()).agregar(tx);
            }
        }

        if (tx.getEstado() == EstadoTransaccion.COMPLETADA) {
            if (tx.isReversible()) {
                pilaReversiones.apilar(tx);
            }
            if (tx.getTipoTransaccion() == TipoTransaccion.TRANSFERENCIA && origen != null && destino != null) {
                grafoTransferencias.agregarArista(origen, destino, tx.getValor());
            }
        }
    }

    public Lista<Transaccion> obtenerListaUsuario(String usuarioId) {
        Lista<Transaccion> lista = historialPorUsuario.obtener(usuarioId);
        if (lista == null) {
            lista = new Lista<>();
            historialPorUsuario.insertar(usuarioId, lista);
        }
        return lista;
    }

    public Lista<Transaccion> obtenerListaBilletera(String codigoBilletera) {
        Lista<Transaccion> lista = historialPorBilletera.obtener(codigoBilletera);
        if (lista == null) {
            lista = new Lista<>();
            historialPorBilletera.insertar(codigoBilletera, lista);
        }
        return lista;
    }

    public Lista<Beneficio> obtenerBeneficiosUsuario(String usuarioId) {
        Lista<Beneficio> lista = beneficiosPorUsuario.obtener(usuarioId);
        if (lista == null) {
            lista = new Lista<>();
            beneficiosPorUsuario.insertar(usuarioId, lista);
        }
        return lista;
    }

    // Getters de los 7 TDA
    public TablaHash<String, Usuario> getUsuariosPorId() { return usuariosPorId; }
    public TablaHash<String, Billetera> getBilleterasPorCodigo() { return billeterasPorCodigo; }
    public ArbolBST<Usuario> getArbolPuntos() { return arbolPuntos; }
    public ColaPrioridad<OperacionProgramada> getColaProgramadas() { return colaProgramadas; }
    public Cola<Notificacion> getColaNotificaciones() { return colaNotificaciones; }
    public Pila<Transaccion> getPilaReversiones() { return pilaReversiones; }
    public Grafo<Billetera> getGrafoTransferencias() { return grafoTransferencias; }
    public Lista<RegistroAuditoria> getHistorialAuditoria() { return historialAuditoria; }
}