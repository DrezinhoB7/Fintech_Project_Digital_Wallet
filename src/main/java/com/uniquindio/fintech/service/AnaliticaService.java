package com.uniquindio.fintech.service;

import com.uniquindio.fintech.config.PlataformaFintechContext;
import com.uniquindio.fintech.dto.AristaTransferenciaDTO;
import com.uniquindio.fintech.dto.NodoGrafoDTO;
import com.uniquindio.fintech.dto.RegistroAuditoriaResponseDTO;
import com.uniquindio.fintech.dto.ResumenRedDTO;
import com.uniquindio.fintech.enums.NivelRiesgo;
import com.uniquindio.fintech.exception.RecursoNoEncontradoException;
import com.uniquindio.fintech.mapper.FintechMapper;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.RegistroAuditoria;
import com.uniquindio.fintech.repository.RegistroAuditoriaRepository;
import com.uniquindio.fintech.repository.UsuarioRepository;
import com.uniquindio.fintech.structures.grafo.Arista;
import com.uniquindio.fintech.structures.grafo.Grafo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnaliticaService {

    private final PlataformaFintechContext context;
    private final RegistroAuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FintechMapper mapper;

    public AnaliticaService(PlataformaFintechContext context,
                            RegistroAuditoriaRepository auditoriaRepository,
                            UsuarioRepository usuarioRepository,
                            FintechMapper mapper) {
        this.context = context;
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AristaTransferenciaDTO> obtenerTodasLasConexiones() {
        Grafo<Billetera> grafo = context.getGrafoTransferencias();
        List<AristaTransferenciaDTO> conexiones = new ArrayList<>();

        for (Billetera origen : grafo.obtenerVertices()) {
            for (Arista<Billetera> arista : grafo.obtenerVecinos(origen)) {
                conexiones.add(mapearArista(origen, arista));
            }
        }
        return conexiones;
    }

    @Transactional(readOnly = true)
    public List<AristaTransferenciaDTO> obtenerConexionesDesdeBilletera(String codigoBilletera) {
        Billetera origen = context.getBilleterasPorCodigo().obtener(codigoBilletera);
        if (origen == null) {
            throw new RecursoNoEncontradoException("Billetera no encontrada con código: " + codigoBilletera);
        }

        List<AristaTransferenciaDTO> conexiones = new ArrayList<>();
        for (Arista<Billetera> arista : context.getGrafoTransferencias().obtenerVecinos(origen)) {
            conexiones.add(mapearArista(origen, arista));
        }
        return conexiones;
    }

    @Transactional(readOnly = true)
    public List<AristaTransferenciaDTO> obtenerRutasFrecuentes(int minFrecuencia) {
        int umbral = Math.max(1, minFrecuencia);
        return obtenerTodasLasConexiones().stream()
                .filter(c -> c.frecuencia() >= umbral)
                .sorted(Comparator.comparingInt(AristaTransferenciaDTO::frecuencia).reversed()
                        .thenComparing(Comparator.comparingDouble(AristaTransferenciaDTO::montoAcumulado).reversed()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> verificarCiclosEnRed() {
        boolean hayCiclos = context.getGrafoTransferencias().detectarCiclos();
        return Map.of(
                "existenCiclosSospechosos", hayCiclos,
                "mensaje", hayCiclos
                        ? "Alerta: Se detectaron ciclos de transferencias circulares en el grafo."
                        : "Sin ciclos detectados: El flujo de transferencias no presenta triangulación circular."
        );
    }

    @Transactional(readOnly = true)
    public ResumenRedDTO obtenerResumenAnaliticoRed(int minFrecuencia) {
        Grafo<Billetera> grafo = context.getGrafoTransferencias();
        List<AristaTransferenciaDTO> todasLasConexiones = obtenerTodasLasConexiones();

        Map<String, Integer> salientes = new HashMap<>();
        Map<String, Integer> entrantes = new HashMap<>();
        Map<String, Double> enviado = new HashMap<>();
        Map<String, Double> recibido = new HashMap<>();

        for (AristaTransferenciaDTO c : todasLasConexiones) {
            salientes.merge(c.codigoOrigen(), 1, Integer::sum);
            entrantes.merge(c.codigoDestino(), 1, Integer::sum);
            enviado.merge(c.codigoOrigen(), c.montoAcumulado(), Double::sum);
            recibido.merge(c.codigoDestino(), c.montoAcumulado(), Double::sum);
        }

        List<NodoGrafoDTO> nodos = new ArrayList<>();
        for (Billetera b : grafo.obtenerVertices()) {
            String cod = b.getCodigo();
            String titular = b.getUsuario() != null ? b.getUsuario().getNombre() : "Sin titular";
            nodos.add(new NodoGrafoDTO(
                    cod,
                    b.getNombre(),
                    titular,
                    salientes.getOrDefault(cod, 0),
                    entrantes.getOrDefault(cod, 0),
                    enviado.getOrDefault(cod, 0.0),
                    recibido.getOrDefault(cod, 0.0)
            ));
        }

        nodos.sort(Comparator.comparingDouble(
                (NodoGrafoDTO n) -> n.montoTotalEnviado() + n.montoTotalRecibido()
        ).reversed());

        return new ResumenRedDTO(
                nodos.size(),
                todasLasConexiones.size(),
                grafo.detectarCiclos(),
                obtenerRutasFrecuentes(minFrecuencia),
                nodos
        );
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoriaResponseDTO> listarHistorialAuditoria() {
        List<RegistroAuditoriaResponseDTO> resultado = new ArrayList<>();
        for (RegistroAuditoria reg : context.getHistorialAuditoria()) {
            resultado.add(mapper.toAuditoriaDTO(reg));
        }

        if (resultado.isEmpty()) {
            for (RegistroAuditoria reg : auditoriaRepository.findAllByOrderByFechaDeteccionDesc()) {
                resultado.add(mapper.toAuditoriaDTO(reg));
            }
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoriaResponseDTO> listarAuditoriaPorRiesgo(NivelRiesgo nivel) {
        return listarHistorialAuditoria().stream()
                .filter(r -> r.nivelRiesgo() == nivel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoriaResponseDTO> listarAuditoriaPorUsuario(String usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId);
        }
        return listarHistorialAuditoria().stream()
                .filter(r -> usuarioId.equals(r.usuarioId()))
                .toList();
    }

    private AristaTransferenciaDTO mapearArista(Billetera origen, Arista<Billetera> arista) {
        Billetera destino = arista.getDestino();
        String titularOrigen = origen.getUsuario() != null ? origen.getUsuario().getNombre() : "N/A";
        String titularDestino = destino.getUsuario() != null ? destino.getUsuario().getNombre() : "N/A";

        return new AristaTransferenciaDTO(
                origen.getCodigo(),
                titularOrigen,
                destino.getCodigo(),
                titularDestino,
                arista.getFrecuencia(),
                arista.getPesoMonto()
        );
    }
}