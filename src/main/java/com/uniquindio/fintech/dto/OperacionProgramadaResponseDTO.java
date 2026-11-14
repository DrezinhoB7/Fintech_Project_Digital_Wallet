package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.EstadoBilletera;
import com.uniquindio.fintech.enums.EstadoTransaccion;
import com.uniquindio.fintech.enums.FrecuenciaOperacion;
import com.uniquindio.fintech.model.Billetera;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record OperacionProgramadaResponseDTO( String id,
                                             LocalDateTime fechaEjecucion,
                                             FrecuenciaOperacion frecuencia,
                                             boolean estado,
                                             Billetera origen,
                                             Billetera Destino) {
}
