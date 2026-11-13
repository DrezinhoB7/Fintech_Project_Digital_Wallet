package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.FrecuenciaOperacion;
import com.uniquindio.fintech.model.Billetera;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record OperacionProgramadaRequestDTO (@NotNull(message = "La billetera es obligatoria") Billetera origen,
                                             @NotNull(message = "La billetera es obligatoria") Billetera Destino,
                                             @Positive(message = "El numero debe ser positivo") double valor,
                                             @NotNull(message = "Debe ingresar una fecha") LocalDateTime fecha,
                                             @NotNull(message = "La frecuencia no debe estar vacia")FrecuenciaOperacion frecuencia){

}


