package com.antecsis.dto.compra;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CompraRequestDTO(
    @NotNull(message = "Proveedor es obligatorio")
    Long proveedorId,

    Long metodoPagoId,
    String observaciones,
    String numeroDocumento,

    @NotEmpty(message = "Debe incluir al menos un item")
    List<@Valid CompraItemDTO> items
) {}
