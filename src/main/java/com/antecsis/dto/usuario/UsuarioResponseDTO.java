package com.antecsis.dto.usuario;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String correo;
    private Integer edad;
    private LocalDate fechaNacimiento;
    private Long sedeId;
    private String sedeNombre;
    private String rolNombre;
    private Boolean activo;
}
