package com.uniquindio.fintech.service;


import com.uniquindio.fintech.config.PlataformaFintechContext;
import com.uniquindio.fintech.dto.OperacionProgramadaRequestDTO;
import com.uniquindio.fintech.dto.OperacionProgramadaResponseDTO;
import com.uniquindio.fintech.enums.EstadoBilletera;
import com.uniquindio.fintech.enums.TipoTransaccion;
import com.uniquindio.fintech.exception.OperacionInvalidaException;
import com.uniquindio.fintech.exception.RecursoNoEncontradoException;
import com.uniquindio.fintech.mapper.FintechMapper;
import com.uniquindio.fintech.model.Billetera;
import com.uniquindio.fintech.model.OperacionProgramada;
import com.uniquindio.fintech.repository.BilleteraRepository;
import com.uniquindio.fintech.repository.OperacionProgramadaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperacionProgramadaService {
    private static final String TIME_ZONE = "America/Bogota";
    @Autowired
    private BilleteraRepository billeteraRepository;
    @Autowired
    private OperacionProgramadaRepository operacionProgramadaRepository;
    private final PlataformaFintechContext context;
    private final FintechMapper mapper;


    public OperacionProgramadaService( PlataformaFintechContext context, FintechMapper mapper) {
        this.context = context;
        this.mapper = mapper;
    }
    @Transactional
    public OperacionProgramadaResponseDTO nuevaOperacionProgramada(OperacionProgramadaRequestDTO dto){
        Billetera billeteraOrigen = billeteraRepository.findById(dto.origen().getCodigo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Billetera no encontrada con el " +
                "Id : "+dto.origen().getCodigo()));
        Billetera billeteraDestino = billeteraRepository.findById(dto.Destino().getCodigo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Billetera no encontrada con el " +
                "Id : "+dto.Destino().getCodigo()));
        if(billeteraOrigen.getEstado().equals(EstadoBilletera.BLOQUEADA) || billeteraOrigen.getEstado().equals(EstadoBilletera.INACTIVA)){
            throw new OperacionInvalidaException("Billetera Origen tiene estado invalido");
        }if(billeteraDestino.getEstado().equals(EstadoBilletera.BLOQUEADA) || billeteraDestino.getEstado().equals(EstadoBilletera.INACTIVA)){
            throw new OperacionInvalidaException("Billetera Destino tiene estado invalido");
        }if(billeteraOrigen.getSaldoDisponible()<dto.valor()){
            throw new OperacionInvalidaException("Billetera con saldo insuficiente");
        }
        billeteraOrigen.setSaldoDisponible(billeteraOrigen.getSaldoDisponible()- dto.valor());
        billeteraDestino.setSaldoDisponible(billeteraDestino.getSaldoDisponible()+ dto.valor());
        OperacionProgramada op = new OperacionProgramada(LocalDateTime.now(), TipoTransaccion.PAGO_PROGRAMADO,
                dto.valor(), dto.frecuencia(), billeteraOrigen, billeteraDestino);
        operacionProgramadaRepository.save(op);
        context.getColaProgramadas().encolar(op,op.getFechaEjecucion().getHour()+op.getFechaEjecucion().getDayOfMonth());
        return mapper.toProgramadaDTO(op);
    }

}
