package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de recuperación de contraseña")
public record ForgotPasswordRequestDTO(
    @Schema(description = "Correo electrónico del usuario", example = "usuario@ejemplo.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    String correo
) {}
