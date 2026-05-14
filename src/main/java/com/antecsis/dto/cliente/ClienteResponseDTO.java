package com.antecsis.dto.cliente;

public record ClienteResponseDTO(
    Long id,
    String nombre,
    String email,
    String telefono,
    String tipoDocumento,
    String documento,
    String direccion,
    String distrito,
    String provincia,
    String departamento,
    String pais,
    Boolean activo
) {}
