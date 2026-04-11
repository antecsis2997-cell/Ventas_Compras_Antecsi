package com.antecsis.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record UsuarioCreateRequest(
    @NotBlank
    String username,

    @NotBlank
    String password,

    @NotBlank
    String rol,

    String nombre,
    String apellido,

    @NotBlank(message = "El correo es obligatorio para poder recuperar la cuenta")
    @Email(message = "El correo no tiene un formato válido")
    String correo,

    LocalDate fechaNacimiento,
    Long sedeId,

    /** Solo para rol SUPERUSUARIO: bodegas licenciadas (debe incluir {@code sedeId} como sede activa). */
    List<Long> sectoresGestionadosIds
) {}
