package com.antecsis.dto.permiso;

public record ModuloDTO(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    String icono,
    Integer orden,
    boolean asignado
) {}
