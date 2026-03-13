package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indica si el usuario puede ver el link Recuperar contraseña")
public record PuedeRecuperarResponseDTO(
    @Schema(description = "Si true, el usuario verá el link cuando falle el login")
    boolean puedeRecuperar
) {}
