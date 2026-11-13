package com.uniquindio.fintech.service;

import com.uniquindio.fintech.config.PlataformaFintechContext;
import com.uniquindio.fintech.dto.BilleteraRequestDTO;
import com.uniquindio.fintech.dto.BilleteraResponseDTO;
import com.uniquindio.fintech.enums.EstadoBilletera;
import com.uniquindio.fintech.exception.OperacionInvalidaException;
import com.uniquindio.fintech.exception.RecursoNoEncontradoException;
import com.uniquindio.fintech.mapper.FintechMapper;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.Usuario;
import com.uniquindio.fintech.repository.BilleteraRepository;
import com.uniquindio.fintech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BilleteraService {

    private final BilleteraRepository billeteraRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlataformaFintechContext context;
    private final FintechMapper mapper;

    public BilleteraService(BilleteraRepository billeteraRepository,
                            UsuarioRepository usuarioRepository,
                            PlataformaFintechContext context,
                            FintechMapper mapper) {
        this.billeteraRepository = billeteraRepository;
        this.usuarioRepository = usuarioRepository;
        this.context = context;
        this.mapper = mapper;
    }

    @Transactional
    public BilleteraResponseDTO crearBilletera(BilleteraRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + dto.usuarioId()));

        if (!usuario.isActivo()) {
            throw new OperacionInvalidaException("No se pueden crear billeteras para un usuario inactivo.");
        }

        String codigo = "BIL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Billetera nueva = new Billetera(codigo, dto.nombre(), dto.tipo(), usuario);
        //Guardamos en la db
        nueva = billeteraRepository.save(nueva);

        //Guardamos/actualizamos en nuestras estructuras propias
        context.getBilleterasPorCodigo().insertar(nueva.getCodigo(), nueva);
        context.getGrafoTransferencias().agregarVertice(nueva);

        return mapper.toBilleteraDTO(nueva);
    }

    @Transactional(readOnly = true)
    public BilleteraResponseDTO consultarPorCodigo(String codigo) {
        Billetera billetera = context.getBilleterasPorCodigo().obtener(codigo);
        if (billetera == null) {
            throw new RecursoNoEncontradoException("Billetera no encontrada con código: " + codigo);
        }
        return mapper.toBilleteraDTO(billetera);
    }

    @Transactional(readOnly = true)
    public List<BilleteraResponseDTO> listarPorUsuario(String usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId);
        }
        return billeteraRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(mapper::toBilleteraDTO)
                .toList();
    }

    @Transactional
    public BilleteraResponseDTO cambiarEstado(String codigo, EstadoBilletera nuevoEstado) {
        Billetera billetera = billeteraRepository.findById(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Billetera no encontrada con código: " + codigo));

        billetera.setEstado(nuevoEstado);
        //Guardamos en la db
        billetera = billeteraRepository.save(billetera);

        //Actualizamos referencia en nuestra tabla hash
        context.getBilleterasPorCodigo().insertar(billetera.getCodigo(), billetera);
        return mapper.toBilleteraDTO(billetera);
    }
}