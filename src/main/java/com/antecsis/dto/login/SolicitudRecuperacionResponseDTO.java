package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Solicitud de recuperación de contraseña pendiente de aprobación")
public record SolicitudRecuperacionResponseDTO(
    @Schema(description = "ID de la solicitud")
    Long id,
    @Schema(description = "Username del usuario que solicitó")
    String username,
    @Schema(description = "Nombre completo del usuario")
    String nombreCompleto,
    @Schema(description = "Correo del usuario")
    String correo,
    @Schema(description = "Fecha de la solicitud")
    Instant fechaSolicitud
) {}
