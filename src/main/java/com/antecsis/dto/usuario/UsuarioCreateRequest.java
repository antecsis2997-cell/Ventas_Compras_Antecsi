package com.antecsis.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class UsuarioCreateRequest {
	@NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String rol;
}
