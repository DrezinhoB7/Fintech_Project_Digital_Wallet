package com.uniquindio.fintech.mapper;

import com.uniquindio.fintech.dto.BilleteraResponseDTO;
import com.uniquindio.fintech.dto.UsuarioResponseDTO;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.Usuario;
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
}