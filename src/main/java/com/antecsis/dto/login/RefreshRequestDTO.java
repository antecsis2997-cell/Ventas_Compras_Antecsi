package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de renovación de token con refresh token")
public record RefreshRequestDTO(
    @NotBlank(message = "El refresh token es requerido")
    @Schema(description = "Refresh token recibido en el login", required = true)
    String refreshToken
) {}
