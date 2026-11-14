package com.uniquindio.fintech.service;

import com.uniquindio.fintech.config.PlataformaFintechContext;
import com.uniquindio.fintech.dto.OperacionProgramadaRequestDTO;
import com.uniquindio.fintech.dto.OperacionProgramadaResponseDTO;
import com.uniquindio.fintech.enums.EstadoBilletera;
import com.uniquindio.fintech.enums.EstadoTransaccion;
import com.uniquindio.fintech.enums.FrecuenciaOperacion;
import com.uniquindio.fintech.enums.TipoAlerta;
import com.uniquindio.fintech.enums.TipoTransaccion;
import com.uniquindio.fintech.exception.OperacionInvalidaException;
import com.uniquindio.fintech.exception.RecursoNoEncontradoException;
import com.uniquindio.fintech.mapper.FintechMapper;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.Notificacion;
import com.uniquindio.fintech.model.OperacionProgramada;
import com.uniquindio.fintech.model.Transaccion;
import com.uniquindio.fintech.model.Usuario;
import com.uniquindio.fintech.repository.BilleteraRepository;
import com.uniquindio.fintech.repository.NotificacionRepository;
import com.uniquindio.fintech.repository.OperacionProgramadaRepository;
import com.uniquindio.fintech.repository.TransaccionRepository;
import com.uniquindio.fintech.repository.UsuarioRepository;
import com.uniquindio.fintech.structures.lista.Lista;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperacionProgramadaService {

    private final BilleteraRepository billeteraRepository;
    private final OperacionProgramadaRepository operacionProgramadaRepository;
    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;
    private final PlataformaFintechContext context;
    private final FintechMapper mapper;

    public OperacionProgramadaService(BilleteraRepository billeteraRepository,
                                      OperacionProgramadaRepository operacionProgramadaRepository,
                                      TransaccionRepository transaccionRepository,
                                      UsuarioRepository usuarioRepository,
                                      NotificacionRepository notificacionRepository,
                                      PlataformaFintechContext context,
                                      FintechMapper mapper) {
        this.billeteraRepository = billeteraRepository;
        this.operacionProgramadaRepository = operacionProgramadaRepository;
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
        this.context = context;
        this.mapper = mapper;
    }

    @Transactional
    public OperacionProgramadaResponseDTO nuevaOperacionProgramada(OperacionProgramadaRequestDTO dto) {
        if (dto.codigoBilleteraOrigen().equals(dto.codigoBilleteraDestino())) {
            throw new OperacionInvalidaException("La billetera de origen y destino no pueden ser la misma.");
        }
        if (dto.fechaEjecucion().isBefore(LocalDateTime.now())) {
            throw new OperacionInvalidaException("La fecha de ejecución debe ser actual o futura.");
        }

        Billetera billeteraOrigen = billeteraRepository.findById(dto.codigoBilleteraOrigen())
                .orElseThrow(() -> new RecursoNoEncontradoException("Billetera de origen no encontrada con el código: " + dto.codigoBilleteraOrigen()));

        Billetera billeteraDestino = billeteraRepository.findById(dto.codigoBilleteraDestino())
                .orElseThrow(() -> new RecursoNoEncontradoException("Billetera de destino no encontrada con el código: " + dto.codigoBilleteraDestino()));

        if (billeteraOrigen.getEstado() != EstadoBilletera.ACTIVA) {
            throw new OperacionInvalidaException("La billetera de origen no se encuentra activa.");
        }
        if (billeteraDestino.getEstado() != EstadoBilletera.ACTIVA) {
            throw new OperacionInvalidaException("La billetera de destino no se encuentra activa.");
        }

        OperacionProgramada op = new OperacionProgramada(
                dto.fechaEjecucion(),
                TipoTransaccion.PAGO_PROGRAMADO,
                dto.valor(),
                dto.frecuencia(),
                billeteraOrigen,
                billeteraDestino
        );
        op = operacionProgramadaRepository.save(op);

        long prioridad = calcularPrioridad(op.getFechaEjecucion());
        context.getColaProgramadas().encolar(op, prioridad);

        return mapper.toProgramadaDTO(op);
    }

    @Transactional(readOnly = true)
    public List<OperacionProgramadaResponseDTO> listarOperacionesPendientes() {
        Lista<OperacionProgramada> enCola = context.getColaProgramadas().recorrer();
        List<OperacionProgramadaResponseDTO> resultado = new ArrayList<>();
        for (OperacionProgramada op : enCola) {
            if (op.isActiva()) {
                resultado.add(mapper.toProgramadaDTO(op));
            }
        }
        return resultado;
    }

    @Transactional
    public List<OperacionProgramadaResponseDTO> ejecutarOperacionesPendientes() {
        LocalDateTime ahora = LocalDateTime.now();
        List<OperacionProgramadaResponseDTO> procesadas = new ArrayList<>();

        while (!context.getColaProgramadas().estaVacia()) {
            OperacionProgramada frente = context.getColaProgramadas().verFrente();
            if (frente.getFechaEjecucion().isAfter(ahora)) {
                break;
            }

            OperacionProgramada op = context.getColaProgramadas().desencolar();
            if (!op.isActiva()) {
                continue;
            }

            procesarEjecucion(op, ahora);
            procesadas.add(mapper.toProgramadaDTO(op));
        }

        return procesadas;
    }

    @Transactional
    public OperacionProgramadaResponseDTO cancelarOperacionProgramada(String id) {
        OperacionProgramada op = operacionProgramadaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Operación programada no encontrada con ID: " + id));

        if (!op.isActiva()) {
            throw new OperacionInvalidaException("La operación programada ya se encuentra inactiva.");
        }

        op.setActiva(false);
        op = operacionProgramadaRepository.save(op);
        sincronizarColaPrioridad();

        return mapper.toProgramadaDTO(op);
    }

    private void procesarEjecucion(OperacionProgramada op, LocalDateTime fechaActual) {
        String codigoOrigen = op.getBilleteraOrigen().getCodigo();
        String codigoDestino = op.getBilleteraDestino().getCodigo();

        Billetera origen = billeteraRepository.findById(codigoOrigen)
                .orElseThrow(() -> new RecursoNoEncontradoException("Billetera origen no encontrada: " + codigoOrigen));
        Billetera destino = billeteraRepository.findById(codigoDestino)
                .orElseThrow(() -> new RecursoNoEncontradoException("Billetera destino no encontrada: " + codigoDestino));

        Transaccion tx = new Transaccion(TipoTransaccion.PAGO_PROGRAMADO, op.getValor(), origen, destino);
        tx.setFecha(fechaActual);

        boolean billeterasActivas = origen.getEstado() == EstadoBilletera.ACTIVA && destino.getEstado() == EstadoBilletera.ACTIVA;
        if (billeterasActivas && origen.retirar(op.getValor())) {
            destino.recargar(op.getValor());
            billeteraRepository.save(origen);
            billeteraRepository.save(destino);

            context.getBilleterasPorCodigo().insertar(origen.getCodigo(), origen);
            context.getBilleterasPorCodigo().insertar(destino.getCodigo(), destino);

            int puntos = tx.calcularPuntos();
            tx.setPuntosGenerados(puntos);
            tx.setEstado(EstadoTransaccion.COMPLETADA);

            Usuario usuarioOrigen = origen.getUsuario();
            context.getArbolPuntos().eliminar(usuarioOrigen);
            boolean subioNivel = usuarioOrigen.acumularPuntos(puntos);
            usuarioOrigen = usuarioRepository.save(usuarioOrigen);
            context.getUsuariosPorId().insertar(usuarioOrigen.getIdentificacion(), usuarioOrigen);
            context.getArbolPuntos().insertar(usuarioOrigen);

            registrarNotificacion(
                    TipoAlerta.OPERACION_PROGRAMADA,
                    "Se ejecutó exitosamente el pago programado por $" + op.getValor() + " hacia la billetera " + destino.getCodigo(),
                    usuarioOrigen
            );

            if (subioNivel) {
                registrarNotificacion(
                        TipoAlerta.ASCENSO_NIVEL,
                        "¡Felicidades! Has ascendido al nivel " + usuarioOrigen.getNivel(),
                        usuarioOrigen
                );
            }
        } else {
            tx.setEstado(EstadoTransaccion.RECHAZADA);
            registrarNotificacion(
                    TipoAlerta.OPERACION_RECHAZADA,
                    "No se pudo ejecutar el pago programado por $" + op.getValor() + " desde la billetera " + origen.getCodigo(),
                    origen.getUsuario()
            );
        }

        tx = transaccionRepository.save(tx);
        context.registrarTransaccionEnEstructuras(tx);

        if (op.getFrecuencia() == FrecuenciaOperacion.UNICA) {
            op.setActiva(false);
            operacionProgramadaRepository.save(op);
        } else {
            LocalDateTime siguienteFecha = calcularSiguienteFecha(op.getFechaEjecucion(), op.getFrecuencia());
            op.setFechaEjecucion(siguienteFecha);
            OperacionProgramada reprogramada = operacionProgramadaRepository.save(op);
            context.getColaProgramadas().encolar(reprogramada, calcularPrioridad(siguienteFecha));
        }
    }

    private LocalDateTime calcularSiguienteFecha(LocalDateTime fechaBase, FrecuenciaOperacion frecuencia) {
        return switch (frecuencia) {
            case DIARIA -> fechaBase.plusDays(1);
            case SEMANAL -> fechaBase.plusWeeks(1);
            case MENSUAL -> fechaBase.plusMonths(1);
            case UNICA -> fechaBase;
        };
    }

    private long calcularPrioridad(LocalDateTime fechaEjecucion) {
        return fechaEjecucion.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void sincronizarColaPrioridad() {
        context.getColaProgramadas().limpiar();
        List<OperacionProgramada> activas = operacionProgramadaRepository.findByActivaTrueOrderByFechaEjecucionAsc();
        for (OperacionProgramada activa : activas) {
            context.getColaProgramadas().encolar(activa, calcularPrioridad(activa.getFechaEjecucion()));
        }
    }

    private void registrarNotificacion(TipoAlerta tipo, String mensaje, Usuario usuario) {
        Notificacion notificacion = new Notificacion(tipo, mensaje, usuario);
        notificacion = notificacionRepository.save(notificacion);
        context.getColaNotificaciones().encolar(notificacion);
    }
}
