package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de login: access token y refresh token para renovar sin volver a iniciar sesión")
public record LoginResponseDTO(
    @Schema(description = "Access token JWT (corta duración). Usar en header: Authorization: Bearer <token>")
    String token,
    @Schema(description = "Refresh token (larga duración). Enviar a POST /api/auth/refresh para obtener nuevo access token")
    String refreshToken,
    @Schema(description = "Segundos hasta que expira el access token")
    long expiresIn
) {}
