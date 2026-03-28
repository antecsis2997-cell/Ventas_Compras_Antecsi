package com.antecsis.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDTO(
    @NotBlank(message = "Nombre es obligatorio")
    String nombre,

    @Email(message = "Email inválido")
    String email,

    String telefono,

    String tipoDocumento,
    String documento,
    String direccion,
    String distrito,
    String provincia,
    String pais
) {}
