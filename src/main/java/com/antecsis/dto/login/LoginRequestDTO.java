package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequestDTO(
    @Schema(description = "Nombre de usuario (login)", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username es obligatorio")
    String username,

    @Schema(description = "Contraseña", example = "********", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password es obligatorio")
    String password
) {}
