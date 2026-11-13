package com.uniquindio.fintech.dto;

import com.uniquindio.fintech.enums.NivelUsuario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BeneficioRequestDTO(@NotNull(message = "El nombre es obligatorio")
                                  @Size(min = 3, max = 30, message = "el nombre debe tener entre 3 y 30 caracteres")
                                  String nombre,
                                  @NotNull(message = "La descripcion es necesaria")
                                  @Size(min = 10, max = 50, message = "La descripcion debe tener entre 10 y 50 caracteres")
                                  String descripcion,
                                  @Positive(message = "Los puntos no pueden ser menores a 1")
                                  int puntosRequeridos
                                  ) {
}
