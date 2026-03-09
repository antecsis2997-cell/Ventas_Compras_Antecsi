package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Usuario actual (autenticado)")
public record MeResponseDTO(
    @Schema(description = "Nombre de usuario (login)")
    String username,

    String nombre,
    String apellido,

    @Schema(description = "Nombre del rol: SUPERUSUARIO, ADMIN, CAJERO, etc.")
    String rolNombre,

    @Schema(description = "ID de la sede asignada (null si no tiene)")
    Long sedeId,

    @Schema(description = "Nombre de la sede")
    String sedeNombre,

    @Schema(description = "Códigos de módulos a los que tiene acceso el usuario")
    Set<String> modulos
) {}
