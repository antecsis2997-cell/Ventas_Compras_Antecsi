package com.antecsis.dto.usuario;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record UsuarioResponseDTO(
    Long id,
    String username,
    String nombre,
    String apellido,
    String correo,
    Integer edad,
    LocalDate fechaNacimiento,
    Long sedeId,
    String sedeNombre,
    String rolNombre,
    Boolean activo,
    Set<String> modulos,
    Boolean puedeRecuperarContrasena,
    /** Vacío salvo rol SUPERUSUARIO cliente. */
    List<Long> sectoresGestionadosIds
) {}
