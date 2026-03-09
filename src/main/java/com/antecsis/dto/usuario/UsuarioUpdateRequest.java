package com.antecsis.dto.usuario;

import java.time.LocalDate;

public record UsuarioUpdateRequest(
    String nombre,
    String apellido,
    String correo,
    LocalDate fechaNacimiento,
    String rol,
    Long sedeId,
    Boolean activo,
    /** Si se envía y no está vacío, se actualiza la contraseña. */
    String password
) {}
