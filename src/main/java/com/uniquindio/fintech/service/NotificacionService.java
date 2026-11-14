package com.uniquindio.fintech.service;

import com.uniquindio.fintech.config.PlataformaFintechContext;
import com.uniquindio.fintech.dto.NotificacionResponseDTO;
import com.uniquindio.fintech.enums.TipoAlerta;
import com.uniquindio.fintech.exception.OperacionInvalidaException;
import com.uniquindio.fintech.exception.RecursoNoEncontradoException;
import com.uniquindio.fintech.mapper.FintechMapper;
import com.uniquindio.fintech.model.Beneficio;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.Notificacion;
import com.uniquindio.fintech.model.OperacionProgramada;
import com.uniquindio.fintech.model.Usuario;
import com.uniquindio.fintech.repository.BilleteraRepository;
import com.uniquindio.fintech.repository.NotificacionRepository;
import com.uniquindio.fintech.repository.UsuarioRepository;
import com.uniquindio.fintech.structures.lista.Lista;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificacionService {

    public static final double UMBRAL_SALDO_BAJO = 10000.0;
    public static final long HORAS_AVISO_PROGRAMADA = 24;

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final BilleteraRepository billeteraRepository;
    private final PlataformaFintechContext context;
    private final FintechMapper mapper;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               UsuarioRepository usuarioRepository,
                               BilleteraRepository billeteraRepository,
                               PlataformaFintechContext context,
                               FintechMapper mapper) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.billeteraRepository = billeteraRepository;
        this.context = context;
        this.mapper = mapper;
    }

    @Transactional
    public NotificacionResponseDTO registrarNotificacion(TipoAlerta tipoAlerta, String mensaje, Usuario usuario) {
        if (usuario == null) {
            throw new OperacionInvalidaException("El usuario de la notificación no puede ser nulo.");
        }
        Notificacion notificacion = new Notificacion(tipoAlerta, mensaje, usuario);
        notificacion = notificacionRepository.save(notificacion);
        context.getColaNotificaciones().encolar(notificacion);
        return mapper.toNotificacionDTO(notificacion);
    }

    @Transactional
    public NotificacionResponseDTO crearNotificacion(String usuarioId, TipoAlerta tipoAlerta, String mensaje) {
        Usuario usuario = obtenerUsuario(usuarioId);
        return registrarNotificacion(tipoAlerta, mensaje, usuario);
    }

    // 1. Alerta automática: Saldo bajo
    @Transactional
    public NotificacionResponseDTO verificarYNotificarSaldoBajo(String usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        double saldoTotal = calcularSaldoTotalUsuario(usuario);
        if (saldoTotal < UMBRAL_SALDO_BAJO) {
            return registrarNotificacion(
                    TipoAlerta.SALDO_BAJO,
                    "Alerta de saldo bajo: Tu saldo total disponible es de $" + saldoTotal
                            + " (inferior al mínimo recomendado de $" + UMBRAL_SALDO_BAJO + ").",
                    usuario
            );
        }
        throw new OperacionInvalidaException("El usuario posee saldo suficiente ($" + saldoTotal + "); no aplica alerta de saldo bajo.");
    }

    @Transactional
    public void verificarSaldoBajoBilletera(Billetera billetera) {
        if (billetera != null && billetera.getSaldoDisponible() < UMBRAL_SALDO_BAJO) {
            registrarNotificacion(
                    TipoAlerta.SALDO_BAJO,
                    "Alerta de saldo bajo: La billetera " + billetera.getCodigo()
                            + " (" + billetera.getNombre() + ") tiene un saldo disponible de $"
                            + billetera.getSaldoDisponible() + ".",
                    billetera.getUsuario()
            );
        }
    }

    // 2. Alerta automática: Transacción programada próxima a ejecutarse
    @Transactional
    public List<NotificacionResponseDTO> notificarOperacionesProgramadasProximas() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limiteProximo = ahora.plusHours(HORAS_AVISO_PROGRAMADA);
        Lista<OperacionProgramada> enCola = context.getColaProgramadas().recorrer();
        List<NotificacionResponseDTO> generadas = new ArrayList<>();

        for (OperacionProgramada op : enCola) {
            if (op.isActiva()
                    && !op.getFechaEjecucion().isBefore(ahora)
                    && !op.getFechaEjecucion().isAfter(limiteProximo)) {
                generadas.add(notificarOperacionProgramadaProxima(op));
            }
        }
        return generadas;
    }

    @Transactional
    public NotificacionResponseDTO notificarOperacionProgramadaProxima(OperacionProgramada op) {
        Usuario usuario = op.getBilleteraOrigen().getUsuario();
        String destinoCodigo = op.getBilleteraDestino() != null ? op.getBilleteraDestino().getCodigo() : "N/A";
        String mensaje = "Tienes una transacción programada próxima a ejecutarse el "
                + op.getFechaEjecucion() + " por $" + op.getValor()
                + " desde la billetera " + op.getBilleteraOrigen().getCodigo()
                + " hacia " + destinoCodigo + ".";
        return registrarNotificacion(TipoAlerta.OPERACION_PROGRAMADA, mensaje, usuario);
    }

    // 3. Alerta automática: Operación rechazada
    @Transactional
    public NotificacionResponseDTO notificarOperacionRechazada(Usuario usuario, String motivo) {
        String mensaje = "Operación rechazada: " + motivo;
        return registrarNotificacion(TipoAlerta.OPERACION_RECHAZADA, mensaje, usuario);
    }

    @Transactional
    public NotificacionResponseDTO notificarOperacionRechazada(String usuarioId, String motivo) {
        Usuario usuario = obtenerUsuario(usuarioId);
        return notificarOperacionRechazada(usuario, motivo);
    }

    // 4. Alerta automática: Ascenso de nivel
    @Transactional
    public NotificacionResponseDTO notificarAscensoNivel(Usuario usuario) {
        String mensaje = "¡Felicidades! Has ascendido al nivel " + usuario.getNivel()
                + " con " + usuario.getPuntosAcumulados() + " puntos acumulados.";
        return registrarNotificacion(TipoAlerta.ASCENSO_NIVEL, mensaje, usuario);
    }

    @Transactional
    public NotificacionResponseDTO notificarAscensoNivel(String usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        return notificarAscensoNivel(usuario);
    }

    // 5. Alerta automática: Canje de beneficio realizado
    @Transactional
    public NotificacionResponseDTO notificarCanjeBeneficio(Usuario usuario, Beneficio beneficio) {
        String mensaje = "Canje de beneficio realizado: Has canjeado '" + beneficio.getNombre()
                + "' por " + beneficio.getCostoPuntos() + " puntos. Puntos restantes: "
                + usuario.getPuntosAcumulados() + ".";
        return registrarNotificacion(TipoAlerta.CANJE_BENEFICIO, mensaje, usuario);
    }

    @Transactional
    public NotificacionResponseDTO notificarCanjeBeneficio(String usuarioId, String nombreBeneficio, int puntosDescontados) {
        Usuario usuario = obtenerUsuario(usuarioId);
        String mensaje = "Canje de beneficio realizado: Has canjeado '" + nombreBeneficio
                + "' por " + puntosDescontados + " puntos. Puntos restantes: "
                + usuario.getPuntosAcumulados() + ".";
        return registrarNotificacion(TipoAlerta.CANJE_BENEFICIO, mensaje, usuario);
    }

    // Evaluación integral de alertas automáticas para un usuario (saldo bajo + operaciones próximas)
    @Transactional
    public List<NotificacionResponseDTO> evaluarAlertasAutomaticasPorUsuario(String usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        List<NotificacionResponseDTO> alertasGeneradas = new ArrayList<>();

        double saldoTotal = calcularSaldoTotalUsuario(usuario);
        if (saldoTotal < UMBRAL_SALDO_BAJO) {
            alertasGeneradas.add(registrarNotificacion(
                    TipoAlerta.SALDO_BAJO,
                    "Alerta de saldo bajo: Tu saldo total disponible es de $" + saldoTotal + ".",
                    usuario
            ));
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limiteProximo = ahora.plusHours(HORAS_AVISO_PROGRAMADA);
        for (OperacionProgramada op : context.getColaProgramadas().recorrer()) {
            if (op.isActiva()
                    && op.getBilleteraOrigen() != null
                    && op.getBilleteraOrigen().getUsuario().getId().equals(usuarioId)
                    && !op.getFechaEjecucion().isBefore(ahora)
                    && !op.getFechaEjecucion().isAfter(limiteProximo)) {
                alertasGeneradas.add(notificarOperacionProgramadaProxima(op));
            }
        }

        return alertasGeneradas;
    }

    // Historial de notificaciones recientes por usuario
    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> obtenerHistorialPorUsuario(String usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("No existe el usuario con ID: " + usuarioId);
        }
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(mapper::toNotificacionDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> obtenerNoLeidasPorUsuario(String usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("No existe el usuario con ID: " + usuarioId);
        }
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .filter(n -> !n.isLeida())
                .map(mapper::toNotificacionDTO)
                .toList();
    }

    // Consulta y procesamiento de la Cola FIFO de notificaciones pendientes
    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarColaNotificacionesPendientes() {
        List<NotificacionResponseDTO> pendientes = new ArrayList<>();
        for (Notificacion n : context.getColaNotificaciones().recorrer()) {
            if (!n.isLeida()) {
                pendientes.add(mapper.toNotificacionDTO(n));
            }
        }
        return pendientes;
    }

    @Transactional
    public NotificacionResponseDTO procesarSiguienteNotificacion() {
        while (!context.getColaNotificaciones().estaVacia()) {
            Notificacion siguiente = context.getColaNotificaciones().desencolar();
            Notificacion enBd = notificacionRepository.findById(siguiente.getId()).orElse(siguiente);
            if (!enBd.isLeida()) {
                enBd.marcarComoLeida();
                enBd = notificacionRepository.save(enBd);
                return mapper.toNotificacionDTO(enBd);
            }
        }
        throw new OperacionInvalidaException("No hay notificaciones pendientes en la cola.");
    }

    @Transactional
    public NotificacionResponseDTO marcarComoLeida(String notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada con ID: " + notificacionId));

        if (!notificacion.isLeida()) {
            notificacion.marcarComoLeida();
            notificacion = notificacionRepository.save(notificacion);
            sincronizarColaNotificaciones();
        }
        return mapper.toNotificacionDTO(notificacion);
    }

    @Transactional
    public List<NotificacionResponseDTO> marcarTodasComoLeidasPorUsuario(String usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("No existe el usuario con ID: " + usuarioId);
        }
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
        for (Notificacion n : notificaciones) {
            if (!n.isLeida()) {
                n.marcarComoLeida();
                notificacionRepository.save(n);
            }
        }
        sincronizarColaNotificaciones();
        return notificaciones.stream()
                .map(mapper::toNotificacionDTO)
                .toList();
    }

    private Usuario obtenerUsuario(String usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + usuarioId));
    }

    private double calcularSaldoTotalUsuario(Usuario usuario) {
        List<Billetera> billeteras = billeteraRepository.findByUsuarioId(usuario.getId());
        if (billeteras != null && !billeteras.isEmpty()) {
            return billeteras.stream().mapToDouble(Billetera::getSaldoDisponible).sum();
        }
        return usuario.calcularSaldoTotal();
    }

    private void sincronizarColaNotificaciones() {
        context.getColaNotificaciones().limpiar();
        for (Notificacion n : notificacionRepository.findAll()) {
            if (!n.isLeida()) {
                context.getColaNotificaciones().encolar(n);
            }
        }
    }
}
