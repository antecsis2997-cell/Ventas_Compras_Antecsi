package com.antecsis.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
	@NotBlank(message = "Username es obligatorio")
	private String username;

	@NotBlank(message = "Password es obligatorio")
    private String password;
}
