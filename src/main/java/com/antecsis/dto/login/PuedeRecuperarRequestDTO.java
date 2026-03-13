package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Consulta si un usuario puede ver el link de recuperar contraseña")
public record PuedeRecuperarRequestDTO(
    @Schema(description = "Username del usuario", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username es obligatorio")
    String username
) {}
