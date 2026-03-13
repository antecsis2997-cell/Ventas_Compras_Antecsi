package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de aprobar solicitud de recuperación")
public record AprobarRecuperacionResponseDTO(
    @Schema(description = "Mensaje de confirmación")
    String mensaje,
    @Schema(description = "Correo al que se envió el enlace")
    String correoEnviado
) {}
