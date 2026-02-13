package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Respuesta de login: token JWT para usar en header Authorization: Bearer <token>")
@Getter
@AllArgsConstructor
public class LoginResponseDTO {

	@Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private String token;
}
