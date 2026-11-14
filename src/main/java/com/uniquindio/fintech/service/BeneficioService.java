package com.uniquindio.fintech.service;

import com.uniquindio.fintech.config.PlataformaFintechContext;
import com.uniquindio.fintech.dto.BeneficioRequestDTO;
import com.uniquindio.fintech.dto.BeneficioResponseDTO;
import com.uniquindio.fintech.dto.CanjeRequestDTO;
import com.uniquindio.fintech.dto.CanjeResponseDTO;
import com.uniquindio.fintech.dto.UsuarioResponseDTO;
import com.uniquindio.fintech.enums.TipoAlerta;
import com.uniquindio.fintech.exception.OperacionInvalidaException;
import com.uniquindio.fintech.exception.RecursoNoEncontradoException;
import com.uniquindio.fintech.mapper.FintechMapper;
import com.uniquindio.fintech.model.Beneficio;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.Notificacion;
import com.uniquindio.fintech.model.Usuario;
import com.uniquindio.fintech.model.UsuarioBeneficio;
import com.uniquindio.fintech.repository.BeneficioRepository;
import com.uniquindio.fintech.repository.BilleteraRepository;
import com.uniquindio.fintech.repository.NotificacionRepository;
import com.uniquindio.fintech.repository.UsuarioBeneficioRepository;
import com.uniquindio.fintech.repository.UsuarioRepository;
import com.uniquindio.fintech.structures.lista.Lista;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BeneficioService {

    private final BeneficioRepository beneficioRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioBeneficioRepository usuarioBeneficioRepository;
    private final BilleteraRepository billeteraRepository;
    private final NotificacionRepository notificacionRepository;
    private final PlataformaFintechContext context;
    private final FintechMapper mapper;

    public BeneficioService(BeneficioRepository beneficioRepository,
                            UsuarioRepository usuarioRepository,
                            UsuarioBeneficioRepository usuarioBeneficioRepository,
                            BilleteraRepository billeteraRepository,
                            NotificacionRepository notificacionRepository,
                            PlataformaFintechContext context,
                            FintechMapper mapper) {
        this.beneficioRepository = beneficioRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioBeneficioRepository = usuarioBeneficioRepository;
        this.billeteraRepository = billeteraRepository;
        this.notificacionRepository = notificacionRepository;
        this.context = context;
        this.mapper = mapper;
    }

    @Transactional
    public BeneficioResponseDTO crearBeneficio(BeneficioRequestDTO dto) {
        Beneficio beneficio = new Beneficio(
                dto.nombre(),
                dto.descripcion(),
                dto.costoPuntos(),
                dto.nivelRequerido()
        );
        beneficio = beneficioRepository.save(beneficio);
        return mapper.toBeneficioDTO(beneficio);
    }

    @Transactional(readOnly = true)
    public List<BeneficioResponseDTO> listarBeneficios() {
        List<BeneficioResponseDTO> resultado = new ArrayList<>();
        for (Beneficio b : beneficioRepository.findAll()) {
            resultado.add(mapper.toBeneficioDTO(b));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<BeneficioResponseDTO> listarBeneficiosDisponiblesParaUsuario(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId));

        List<BeneficioResponseDTO> disponibles = new ArrayList<>();
        for (Beneficio b : beneficioRepository.findAll()) {
            if (b.esAplicable(usuario)) {
                disponibles.add(mapper.toBeneficioDTO(b));
            }
        }
        return disponibles;
    }

    @Transactional
    public CanjeResponseDTO canjearBeneficio(CanjeRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + dto.usuarioId()));

        Beneficio beneficio = beneficioRepository.findById(dto.beneficioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Beneficio no encontrado con ID: " + dto.beneficioId()));

        if (!beneficio.esAplicable(usuario)) {
            throw new OperacionInvalidaException(
                    "No cumples los requisitos (puntos suficientes, nivel " + beneficio.getNivelRequerido() + " o cuenta activa) para este beneficio."
            );
        }
        context.getArbolPuntos().eliminar(usuario);

        usuario.canjearBeneficio(beneficio);
        usuario = usuarioRepository.save(usuario);

        context.getUsuariosPorId().insertar(usuario.getIdentificacion(), usuario);
        context.getArbolPuntos().insertar(usuario);

        UsuarioBeneficio canje = new UsuarioBeneficio(usuario, beneficio, beneficio.getCostoPuntos());
        canje = usuarioBeneficioRepository.save(canje);
        context.obtenerBeneficiosUsuario(usuario.getId()).agregar(beneficio);

        Notificacion notificacion = new Notificacion(
                TipoAlerta.CANJE_BENEFICIO,
                "Canjeaste '" + beneficio.getNombre() + "' por " + beneficio.getCostoPuntos() + " puntos.",
                usuario
        );
        notificacion = notificacionRepository.save(notificacion);
        context.getColaNotificaciones().encolar(notificacion);

        return mapper.toCanjeDTO(canje);
    }

    @Transactional(readOnly = true)
    public List<BeneficioResponseDTO> listarBeneficiosCanjeadosPorUsuario(String usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId);
        }

        Lista<Beneficio> canjeados = context.obtenerBeneficiosUsuario(usuarioId);
        List<BeneficioResponseDTO> resultado = new ArrayList<>();
        for (Beneficio b : canjeados) {
            resultado.add(mapper.toBeneficioDTO(b));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerRankingUsuarios() {
        Lista<Usuario> ordenadosMenorAMayor = context.getArbolPuntos().inOrden();
        List<Billetera> todasLasBilleteras = billeteraRepository.findAll();

        List<UsuarioResponseDTO> ranking = new ArrayList<>();
        for (Usuario u : ordenadosMenorAMayor) {
            List<Billetera> billeterasUsuario = todasLasBilleteras.stream()
                    .filter(b -> b.getUsuario() != null && b.getUsuario().getId().equals(u.getId()))
                    .toList();

            ranking.add(0, mapper.toUsuarioDTO(u, billeterasUsuario));
        }
        return ranking;
    }
}