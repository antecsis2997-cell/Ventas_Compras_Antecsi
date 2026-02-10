package com.antecsis.dto.metodopago;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetodoPagoRequestDTO {
    @NotBlank(message = "Nombre es obligatorio")
    private String nombre;
}
