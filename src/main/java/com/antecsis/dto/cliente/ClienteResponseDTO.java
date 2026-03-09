package com.antecsis.dto.cliente;

public record ClienteResponseDTO(
    Long id,
    String nombre,
    String email,
    String telefono,
    String tipoDocumento,
    String documento,
    String direccion,
    Boolean activo
) {}
