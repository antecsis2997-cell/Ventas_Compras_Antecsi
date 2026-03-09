package com.antecsis.dto.metodopago;

import jakarta.validation.constraints.NotBlank;

public record MetodoPagoRequestDTO(
    @NotBlank(message = "Nombre es obligatorio")
    String nombre
) {}
