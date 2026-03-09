package com.antecsis.dto.proveedor;

public record ProveedorResponseDTO(
    Long id,
    String nombre,
    String ruc,
    String email,
    String telefono,
    String direccion,
    Boolean activo
) {}
