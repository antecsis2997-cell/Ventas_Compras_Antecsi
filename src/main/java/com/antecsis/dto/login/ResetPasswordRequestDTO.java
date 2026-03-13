package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Restablecer contraseña con token")
public record ResetPasswordRequestDTO(
    @Schema(description = "Token recibido por correo", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Token es obligatorio")
    String token,

    @Schema(description = "Nueva contraseña (mínimo 6 caracteres)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String nuevaContrasena
) {}
