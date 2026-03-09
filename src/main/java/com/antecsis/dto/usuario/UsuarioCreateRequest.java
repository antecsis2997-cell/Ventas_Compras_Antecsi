package com.antecsis.dto.usuario;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UsuarioCreateRequest(
    @NotBlank
    String username,

    @NotBlank
    String password,

    @NotBlank
    String rol,

    String nombre,
    String apellido,
    String correo,
    LocalDate fechaNacimiento,
    Long sedeId
) {}
