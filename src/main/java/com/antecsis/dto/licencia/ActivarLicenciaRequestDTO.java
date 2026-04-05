package com.antecsis.dto.licencia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Pegue el token recibido por correo")
public record ActivarLicenciaRequestDTO(
        @NotBlank(message = "El token de licencia es obligatorio")
        String token
) {}
