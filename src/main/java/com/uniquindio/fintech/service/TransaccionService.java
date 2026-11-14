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
import com.uniquindio.fintech.model.Transaccion;
import com.uniquindio.fintech.model.Usuario;
import com.uniquindio.fintech.repository.BilleteraRepository;
import com.uniquindio.fintech.repository.TransaccionRepository;
import com.uniquindio.fintech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransaccionService {

    private static final double UMBRAL_RIESGO_MEDIO = 2000.0;
    private static final double UMBRAL_RIESGO_ALTO = 5000.0;

    private final TransaccionRepository transaccionRepository;
    private final BilleteraRepository billeteraRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlataformaFintechContext context;
    private final FintechMapper mapper;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              BilleteraRepository billeteraRepository,
                              UsuarioRepository usuarioRepository,
                              PlataformaFintechContext context,
                              FintechMapper mapper) {
        this.transaccionRepository = transaccionRepository;
        this.billeteraRepository = billeteraRepository;
        this.usuarioRepository = usuarioRepository;
        this.context = context;
        this.mapper = mapper;
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

        return mapper.toTransaccionResponseDTO(tx);
    }

    @Transactional
    public TransaccionResponseDTO retirar(RecargaRetiroRequestDTO dto) {
        Billetera origen = obtenerBilleteraActiva(dto.codigoBilletera());

        if (origen.getSaldoDisponible() < dto.valor()) {
            throw new OperacionInvalidaException("Saldo insuficiente en la billetera " + origen.getCodigo());
        }

        origen.setSaldoDisponible(origen.getSaldoDisponible() - dto.valor());
        billeteraRepository.save(origen);

        Transaccion tx = new Transaccion(TipoTransaccion.RETIRO, dto.valor(), origen, null);
        tx.setEstado(EstadoTransaccion.COMPLETADA);
        evaluarRiesgoYAsignarPuntos(tx, origen.getUsuario());

        tx = transaccionRepository.save(tx);
        context.registrarTransaccionEnEstructuras(tx);

        return mapper.toTransaccionResponseDTO(tx);
    }

    @Transactional
    public TransaccionResponseDTO transferir(TransferenciaRequestDTO dto) {
        if (dto.codigoOrigen().equalsIgnoreCase(dto.codigoDestino())) {
            throw new OperacionInvalidaException("La billetera origen y destino no pueden ser la misma.");
        }

        Billetera origen = obtenerBilleteraActiva(dto.codigoOrigen());
        Billetera destino = obtenerBilleteraActiva(dto.codigoDestino());

        if (origen.getSaldoDisponible() < dto.valor()) {
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

        // El constructor de Transaccion deja reversible = false para REVERSION, así que no se apila
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
            usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() + puntos);
            usuarioRepository.save(usuario);
            context.getArbolPuntos().insertar(usuario);
        }
    }
}