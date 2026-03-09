package com.antecsis.dto.usuario;

/**
 * DTO para autocompletado de usuarios por correo (ej. selector de remitente en solicitudes).
 */
public record UsuarioCorreoDTO(
    String correo,
    String nombre
) {}
