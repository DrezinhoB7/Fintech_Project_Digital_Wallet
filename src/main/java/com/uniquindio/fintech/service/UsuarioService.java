package com.uniquindio.fintech.service;

import com.uniquindio.fintech.config.PlataformaFintechContext;
import com.uniquindio.fintech.dto.UsuarioRequestDTO;
import com.uniquindio.fintech.dto.UsuarioResponseDTO;
import com.uniquindio.fintech.enums.TipoBilletera;
import com.uniquindio.fintech.exception.OperacionInvalidaException;
import com.uniquindio.fintech.exception.RecursoNoEncontradoException;
import com.uniquindio.fintech.mapper.FintechMapper;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.Usuario;
import com.uniquindio.fintech.repository.BilleteraRepository;
import com.uniquindio.fintech.repository.UsuarioRepository;
import com.uniquindio.fintech.structures.lista.Lista;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BilleteraRepository billeteraRepository;
    private final PlataformaFintechContext context;
    private final FintechMapper mapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          BilleteraRepository billeteraRepository,
                          PlataformaFintechContext context,
                          FintechMapper mapper) {
        this.usuarioRepository = usuarioRepository;
        this.billeteraRepository = billeteraRepository;
        this.context = context;
        this.mapper = mapper;
    }

    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto) {
        if (context.getUsuariosPorId().contieneClave(dto.identificacion())
                || usuarioRepository.existsByIdentificacion(dto.identificacion())) {
            throw new OperacionInvalidaException("Ya existe un usuario registrado con la identificación: " + dto.identificacion());
        }
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new OperacionInvalidaException("Ya existe un usuario registrado con el correo: " + dto.email());
        }

        Usuario nuevoUsuario = new Usuario(
                dto.identificacion(),
                dto.nombre(),
                dto.email(),
                dto.telefono()
        );
        nuevoUsuario = usuarioRepository.save(nuevoUsuario);

        String codigoInicial = generarCodigoBilletera();
        Billetera billeteraInicial = new Billetera(
                codigoInicial,
                "Billetera Principal",
                TipoBilletera.GASTOS_DIARIOS,
                nuevoUsuario
        );
        billeteraInicial = billeteraRepository.save(billeteraInicial);
        nuevoUsuario.getBilleteras().add(billeteraInicial);

        // Sincronización con los TDA propios en memoria
        context.getUsuariosPorId().insertar(nuevoUsuario.getIdentificacion(), nuevoUsuario);
        context.getArbolPuntos().insertar(nuevoUsuario);
        context.getBilleterasPorCodigo().insertar(billeteraInicial.getCodigo(), billeteraInicial);
        context.getGrafoTransferencias().agregarVertice(billeteraInicial);

        return mapper.toUsuarioDTO(nuevoUsuario, List.of(billeteraInicial));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios() {
        List<Usuario> usuariosBD = usuarioRepository.findAll();
        List<UsuarioResponseDTO> respuesta = new ArrayList<>();
        for (Usuario u : usuariosBD) {
            if (u.isActivo()) {
                context.getUsuariosPorId().insertar(u.getIdentificacion(), u);
                List<Billetera> billeteras = billeteraRepository.findByUsuarioId(u.getId());
                respuesta.add(mapper.toUsuarioDTO(u, billeteras));
            }
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorIdentificacion(String identificacion) {
        // Consulta O(1) promedio mediante nuestra TablaHash propia
        Usuario usuario = context.getUsuariosPorId().obtener(identificacion);
        if (usuario == null) {
            throw new RecursoNoEncontradoException("No se encontró ningún usuario con identificación: " + identificacion);
        }
        List<Billetera> billeteras = billeteraRepository.findByUsuarioId(usuario.getId());
        return mapper.toUsuarioDTO(usuario, billeteras);
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(String id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        if (!usuario.getEmail().equalsIgnoreCase(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new OperacionInvalidaException("El correo electrónico ya está en uso por otro usuario.");
        }

        // Si cambia la identificación, actualizamos la clave en TablaHash y posición en ArbolBST
        String identificacionAnterior = usuario.getIdentificacion();
        if (!identificacionAnterior.equals(dto.identificacion())) {
            if (usuarioRepository.existsByIdentificacion(dto.identificacion())) {
                throw new OperacionInvalidaException("La nueva identificación ya pertenece a otro usuario.");
            }
            context.getUsuariosPorId().eliminar(identificacionAnterior);
            context.getArbolPuntos().eliminar(usuario);
            usuario.setIdentificacion(dto.identificacion());
        }

        usuario.setNombre(dto.nombre());
        usuario.setEmail(dto.email());
        usuario.setTelefono(dto.telefono());
        usuario = usuarioRepository.save(usuario);

        context.getUsuariosPorId().insertar(usuario.getIdentificacion(), usuario);
        if (!identificacionAnterior.equals(usuario.getIdentificacion())) {
            context.getArbolPuntos().insertar(usuario);
        }

        List<Billetera> billeteras = billeteraRepository.findByUsuarioId(usuario.getId());
        return mapper.toUsuarioDTO(usuario, billeteras);
    }

    @Transactional
    public void eliminarUsuario(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        List<Billetera> billeteras = billeteraRepository.findByUsuarioId(id);
        double saldoTotal = billeteras.stream().mapToDouble(Billetera::getSaldoDisponible).sum();
        if (saldoTotal > 0) {
            throw new OperacionInvalidaException("No se puede eliminar un usuario que aún posee saldo disponible ($" + saldoTotal + ").");
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        context.getUsuariosPorId().eliminar(usuario.getIdentificacion());
        context.getArbolPuntos().eliminarSi(u -> u.getId().equals(id));
    }

    private String generarCodigoBilletera() {
        return "BIL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}