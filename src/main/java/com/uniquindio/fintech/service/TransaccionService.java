package com.uniquindio.fintech.service;

import com.uniquindio.fintech.config.PlataformaFintechContext;
import com.uniquindio.fintech.dto.RecargaRetiroRequestDTO;
import com.uniquindio.fintech.dto.TransaccionResponseDTO;
import com.uniquindio.fintech.dto.TransferenciaRequestDTO;
import com.uniquindio.fintech.enums.EstadoBilletera;
import com.uniquindio.fintech.enums.EstadoTransaccion;
import com.uniquindio.fintech.enums.NivelRiesgo;
import com.uniquindio.fintech.enums.TipoTransaccion;
import com.uniquindio.fintech.exception.OperacionInvalidaException;
import com.uniquindio.fintech.exception.RecursoNoEncontradoException;
import com.uniquindio.fintech.mapper.FintechMapper;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.RegistroAuditoria;
import com.uniquindio.fintech.model.Transaccion;
import com.uniquindio.fintech.model.Usuario;
import com.uniquindio.fintech.repository.BilleteraRepository;
import com.uniquindio.fintech.repository.RegistroAuditoriaRepository;
import com.uniquindio.fintech.repository.TransaccionRepository;
import com.uniquindio.fintech.repository.UsuarioRepository;
import com.uniquindio.fintech.structures.grafo.Arista;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TransaccionService {

    private static final double UMBRAL_RIESGO_MEDIO = 2000.0;
    private static final double UMBRAL_RIESGO_ALTO = 5000.0;
    private static final int UMBRAL_RAFAGA_TRANSFERENCIAS = 3;

    private final TransaccionRepository transaccionRepository;
    private final BilleteraRepository billeteraRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroAuditoriaRepository auditoriaRepository;
    private final PlataformaFintechContext context;
    private final FintechMapper mapper;
    private final NotificacionService notificacionService;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              BilleteraRepository billeteraRepository,
                              UsuarioRepository usuarioRepository,
                              RegistroAuditoriaRepository auditoriaRepository,
                              PlataformaFintechContext context,
                              FintechMapper mapper,
                              NotificacionService notificacionService) {
        this.transaccionRepository = transaccionRepository;
        this.billeteraRepository = billeteraRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.context = context;
        this.mapper = mapper;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public TransaccionResponseDTO recargar(RecargaRetiroRequestDTO dto) {
        Billetera destino = obtenerBilleteraActiva(dto.codigoBilletera());

        destino.setSaldoDisponible(destino.getSaldoDisponible() + dto.valor());
        billeteraRepository.save(destino);

        Transaccion tx = new Transaccion(TipoTransaccion.RECARGA, dto.valor(), null, destino);
        tx.setEstado(EstadoTransaccion.COMPLETADA);
        evaluarRiesgoYAsignarPuntos(tx, destino.getUsuario());

        tx = transaccionRepository.save(tx);
        context.registrarTransaccionEnEstructuras(tx);
        auditarTransaccion(tx, destino.getUsuario());

        return mapper.toTransaccionResponseDTO(tx);
    }

    @Transactional(noRollbackFor = OperacionInvalidaException.class)
    public TransaccionResponseDTO retirar(RecargaRetiroRequestDTO dto) {
        Billetera origen = obtenerBilleteraActiva(dto.codigoBilletera());

        if (origen.getSaldoDisponible() < dto.valor()) {
            notificacionService.notificarOperacionRechazada(
                    origen.getUsuario(),
                    "Retiro por $" + dto.valor() + " rechazado por saldo insuficiente en la billetera " + origen.getCodigo()
            );
            throw new OperacionInvalidaException("Saldo insuficiente en la billetera " + origen.getCodigo());
        }

        origen.setSaldoDisponible(origen.getSaldoDisponible() - dto.valor());
        billeteraRepository.save(origen);

        Transaccion tx = new Transaccion(TipoTransaccion.RETIRO, dto.valor(), origen, null);
        tx.setEstado(EstadoTransaccion.COMPLETADA);
        evaluarRiesgoYAsignarPuntos(tx, origen.getUsuario());

        tx = transaccionRepository.save(tx);
        context.registrarTransaccionEnEstructuras(tx);
        auditarTransaccion(tx, origen.getUsuario());
        notificacionService.verificarSaldoBajoBilletera(origen);

        return mapper.toTransaccionResponseDTO(tx);
    }

    @Transactional(noRollbackFor = OperacionInvalidaException.class)
    public TransaccionResponseDTO transferir(TransferenciaRequestDTO dto) {
        if (dto.codigoOrigen().equalsIgnoreCase(dto.codigoDestino())) {
            throw new OperacionInvalidaException("La billetera origen y destino no pueden ser la misma.");
        }

        Billetera origen = obtenerBilleteraActiva(dto.codigoOrigen());
        Billetera destino = obtenerBilleteraActiva(dto.codigoDestino());

        if (origen.getSaldoDisponible() < dto.valor()) {
            notificacionService.notificarOperacionRechazada(
                    origen.getUsuario(),
                    "Transferencia por $" + dto.valor() + " rechazada por saldo insuficiente en la billetera " + origen.getCodigo()
            );
            throw new OperacionInvalidaException("Saldo insuficiente para realizar la transferencia.");
        }

        origen.setSaldoDisponible(origen.getSaldoDisponible() - dto.valor());
        destino.setSaldoDisponible(destino.getSaldoDisponible() + dto.valor());
        billeteraRepository.save(origen);
        billeteraRepository.save(destino);

        Transaccion tx = new Transaccion(TipoTransaccion.TRANSFERENCIA, dto.valor(), origen, destino);
        tx.setEstado(EstadoTransaccion.COMPLETADA);
        evaluarRiesgoYAsignarPuntos(tx, origen.getUsuario());

        tx = transaccionRepository.save(tx);
        context.registrarTransaccionEnEstructuras(tx);
        auditarTransaccion(tx, origen.getUsuario());
        notificacionService.verificarSaldoBajoBilletera(origen);

        return mapper.toTransaccionResponseDTO(tx);
    }

    public List<TransaccionResponseDTO> obtenerHistorialPorBilletera(String codigoBilletera) {
        Billetera billetera = context.getBilleterasPorCodigo().obtener(codigoBilletera);
        if (billetera == null) {
            throw new RecursoNoEncontradoException("No existe la billetera con código: " + codigoBilletera);
        }

        List<TransaccionResponseDTO> resultado = new ArrayList<>();
        for (Transaccion tx : context.obtenerListaBilletera(codigoBilletera)) {
            resultado.add(mapper.toTransaccionResponseDTO(tx));
        }
        return resultado;
    }

    public List<TransaccionResponseDTO> obtenerHistorialPorUsuario(String usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("No existe el usuario con id: " + usuarioId);
        }

        List<TransaccionResponseDTO> resultado = new ArrayList<>();
        for (Transaccion tx : context.obtenerListaUsuario(usuarioId)) {
            resultado.add(mapper.toTransaccionResponseDTO(tx));
        }
        return resultado;
    }

    @Transactional
    public TransaccionResponseDTO revertirUltimaTransaccion() {
        if (context.getPilaReversiones().estaVacia()) {
            throw new OperacionInvalidaException("No hay transacciones reversibles en la pila.");
        }

        Transaccion ultima = context.getPilaReversiones().desapilar();
        if (!ultima.isReversible() || ultima.getEstado() == EstadoTransaccion.REVERTIDA) {
            throw new OperacionInvalidaException("La transacción seleccionada no puede ser revertida.");
        }

        Billetera origen = ultima.getBilleteraOrigen();
        Billetera destino = ultima.getBilleteraDestino();
        double valor = ultima.getValor();

        switch (ultima.getTipoTransaccion()) {
            case RECARGA -> {
                if (destino.getSaldoDisponible() < valor) {
                    context.getPilaReversiones().apilar(ultima);
                    throw new OperacionInvalidaException("No se puede revertir la recarga: saldo insuficiente en destino.");
                }
                destino.setSaldoDisponible(destino.getSaldoDisponible() - valor);
                billeteraRepository.save(destino);
            }
            case TRANSFERENCIA -> {
                if (destino.getSaldoDisponible() < valor) {
                    context.getPilaReversiones().apilar(ultima);
                    throw new OperacionInvalidaException("No se puede revertir la transferencia: el destinatario ya usó el saldo.");
                }
                destino.setSaldoDisponible(destino.getSaldoDisponible() - valor);
                origen.setSaldoDisponible(origen.getSaldoDisponible() + valor);
                billeteraRepository.save(destino);
                billeteraRepository.save(origen);
            }
            default -> throw new OperacionInvalidaException("Este tipo de transacción no admite reversión.");
        }

        ultima.setEstado(EstadoTransaccion.REVERTIDA);
        ultima.setReversible(false);
        transaccionRepository.save(ultima);

        Transaccion reversion = new Transaccion(TipoTransaccion.REVERSION, valor, destino, origen);
        reversion.setEstado(EstadoTransaccion.COMPLETADA);
        reversion = transaccionRepository.save(reversion);
        context.registrarTransaccionEnEstructuras(reversion);

        return mapper.toTransaccionResponseDTO(reversion);
    }

    private Billetera obtenerBilleteraActiva(String codigo) {
        Billetera billetera = context.getBilleterasPorCodigo().obtener(codigo);
        if (billetera == null) {
            throw new RecursoNoEncontradoException("Billetera no encontrada con código: " + codigo);
        }
        if (billetera.getEstado() != EstadoBilletera.ACTIVA) {
            throw new OperacionInvalidaException("La billetera " + codigo + " no se encuentra ACTIVA.");
        }
        return billetera;
    }

    private void evaluarRiesgoYAsignarPuntos(Transaccion tx, Usuario usuario) {
        if (tx.getValor() >= UMBRAL_RIESGO_ALTO) {
            tx.setNivelRiesgo(NivelRiesgo.ALTO);
        } else if (tx.getValor() >= UMBRAL_RIESGO_MEDIO) {
            tx.setNivelRiesgo(NivelRiesgo.MEDIO);
        } else {
            tx.setNivelRiesgo(NivelRiesgo.BAJO);
        }

        int puntos = tx.calcularPuntos();
        tx.setPuntosGenerados(puntos);

        if (puntos > 0 && usuario != null) {
            context.getArbolPuntos().eliminar(usuario);
            boolean subioNivel = usuario.acumularPuntos(puntos);
            usuarioRepository.save(usuario);
            context.getArbolPuntos().insertar(usuario);

            if (subioNivel) {
                notificacionService.notificarAscensoNivel(usuario);
            }
        }
    }

    private void auditarTransaccion(Transaccion tx, Usuario usuario) {
        if (usuario == null) {
            return;
        }

        //peración por monto elevado
        if (tx.getNivelRiesgo() == NivelRiesgo.ALTO) {
            registrarAlertaAuditoria(
                    "MONTO_ELEVADO",
                    "Operación tipo " + tx.getTipoTransaccion() + " por valor de $" + tx.getValor()
                            + " supera el umbral de control ($" + UMBRAL_RIESGO_ALTO + ").",
                    NivelRiesgo.ALTO,
                    tx,
                    usuario
            );
        }


        if (tx.getTipoTransaccion() == TipoTransaccion.TRANSFERENCIA
                && tx.getBilleteraOrigen() != null
                && tx.getBilleteraDestino() != null) {

            Billetera origen = tx.getBilleteraOrigen();
            Billetera destino = tx.getBilleteraDestino();

            //Muchas transferencias entre las dos mismas billeteras
            for (Arista<Billetera> arista : context.getGrafoTransferencias().obtenerVecinos(origen)) {
                if (Objects.equals(arista.getDestino(), destino)
                        && arista.getFrecuencia() >= UMBRAL_RAFAGA_TRANSFERENCIAS) {
                    registrarAlertaAuditoria(
                            "RAFAGA_TRANSFERENCIAS",
                            "Se registran " + arista.getFrecuencia() + " transferencias entre "
                                    + origen.getCodigo() + " y " + destino.getCodigo()
                                    + " (acumulado: $" + arista.getPesoMonto() + ").",
                            NivelRiesgo.MEDIO,
                            tx,
                            usuario
                    );
                    break;
                }
            }

            //Triangulación circular en tiempo real
            if (context.getGrafoTransferencias().detectarCiclos()) {
                registrarAlertaAuditoria(
                        "CICLO_TRANSFERENCIA_DETECTADO",
                        "Triangulación circular detectada en el grafo tras transferencia de "
                                + origen.getCodigo() + " hacia " + destino.getCodigo() + ".",
                        NivelRiesgo.ALTO,
                        tx,
                        usuario
                );
            }
        }
    }

    private void registrarAlertaAuditoria(String regla, String descripcion, NivelRiesgo riesgo,
                                          Transaccion tx, Usuario usuario) {
        RegistroAuditoria registro = new RegistroAuditoria(regla, descripcion, riesgo, tx, usuario);
        registro = auditoriaRepository.save(registro);
        context.getHistorialAuditoria().agregar(registro);
    }
}