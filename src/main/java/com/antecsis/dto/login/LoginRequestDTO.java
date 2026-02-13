package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Credenciales para iniciar sesión")
@Getter
@Setter
public class LoginRequestDTO {

	@Schema(description = "Nombre de usuario (login)", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Username es obligatorio")
	private String username;

	@Schema(description = "Contraseña", example = "********", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Password es obligatorio")
	private String password;
}
