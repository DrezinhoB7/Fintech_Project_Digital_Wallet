package com.uniquindio.fintech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @NotBlank(message = "La identificación es obligatoria.")
        @Size(min = 5, max = 20, message = "La identificación debe tener entre 5 y 20 caracteres.")
        String identificacion,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
        String nombre,

        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El formato del correo electrónico no es válido.")
        String email,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.")
        String telefono
) {
}