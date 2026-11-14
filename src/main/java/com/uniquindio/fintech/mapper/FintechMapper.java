package com.uniquindio.fintech.mapper;

import com.uniquindio.fintech.dto.*;
import com.uniquindio.fintech.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FintechMapper {

    public UsuarioResponseDTO toUsuarioDTO(Usuario usuario, List<Billetera> billeterasUsuario) {
        int cantidad = billeterasUsuario != null ? billeterasUsuario.size() : 0;
        double saldoTotal = billeterasUsuario != null
                ? billeterasUsuario.stream().mapToDouble(Billetera::getSaldoDisponible).sum()
                : 0.0;

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getIdentificacion(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getPuntosAcumulados(),
                usuario.getNivel(),
                usuario.isActivo(),
                usuario.getFechaRegistro(),
                cantidad,
                saldoTotal
        );
    }

    public BilleteraResponseDTO toBilleteraDTO(Billetera billetera) {
        return new BilleteraResponseDTO(
                billetera.getCodigo(),
                billetera.getNombre(),
                billetera.getTipo(),
                billetera.getSaldoDisponible(),
                billetera.getEstado(),
                billetera.getUsuario().getId(),
                billetera.getUsuario().getNombre()
        );
    }

    public TransaccionResponseDTO toTransaccionResponseDTO(Transaccion tx) {
        String codOrigen = (tx.getBilleteraOrigen() != null) ? tx.getBilleteraOrigen().getCodigo() : null;
        String codDestino = (tx.getBilleteraDestino() != null) ? tx.getBilleteraDestino().getCodigo() : null;

        return new TransaccionResponseDTO(
                tx.getId(),
                tx.getFecha(),
                tx.getTipoTransaccion(),
                tx.getValor(),
                tx.getEstado(),
                tx.getPuntosGenerados(),
                tx.getNivelRiesgo(),
                tx.isReversible(),
                codOrigen,
                codDestino
        );
    }


    public OperacionProgramadaResponseDTO toProgramadaDTO(OperacionProgramada op) {
        return new OperacionProgramadaResponseDTO(
                op.getId(),
                op.getFechaEjecucion(),
                op.getTipoOperacion(),
                op.getValor(),
                op.getFrecuencia(),
                op.isActiva(),
                op.getBilleteraOrigen() != null ? op.getBilleteraOrigen().getCodigo() : null,
                op.getBilleteraDestino() != null ? op.getBilleteraDestino().getCodigo() : null
        );
    }

    public BeneficioResponseDTO toBeneficioDTO(Beneficio beneficio) {
        return new BeneficioResponseDTO(
                beneficio.getId(),
                beneficio.getNombre(),
                beneficio.getDescripcion(),
                beneficio.getCostoPuntos(),
                beneficio.getNivelRequerido()
        );
    }

    public CanjeResponseDTO toCanjeDTO(UsuarioBeneficio canje) {
        Usuario u = canje.getUsuario();
        Beneficio b = canje.getBeneficio();

        return new CanjeResponseDTO(
                canje.getId(),
                u.getId(),
                u.getNombre(),
                b.getId(),
                b.getNombre(),
                canje.getPuntosDescontados(),
                u.getPuntosAcumulados(),
                u.getNivel(),
                canje.getFechaCanje()
        );
    }

    public NotificacionResponseDTO toNotificacionDTO(Notificacion notificacion) {
        return new NotificacionResponseDTO(
                notificacion.getId(),
                notificacion.getTipoAlerta(),
                notificacion.getMensaje(),
                notificacion.getFecha(),
                notificacion.isLeida(),
                notificacion.getUsuario()
        );
    }
}