package com.antecsis.dto.usuario;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioUpdateRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private LocalDate fechaNacimiento;
    private String rol;
    private Long sedeId;
    private Boolean activo;
    /** Si se envía y no está vacío, se actualiza la contraseña. */
    private String password;
}
