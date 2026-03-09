package com.antecsis.dto.proveedor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProveedorRequestDTO(
    @NotBlank(message = "Nombre es obligatorio")
    String nombre,

    String ruc,

    @Email(message = "Email inválido")
    String email,

    String telefono,
    String direccion
) {}
