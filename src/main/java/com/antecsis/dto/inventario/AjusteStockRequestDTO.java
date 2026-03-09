package com.antecsis.dto.inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AjusteStockRequestDTO(
    @NotNull(message = "Producto es obligatorio")
    Long productoId,

    @NotNull(message = "Nuevo stock es obligatorio")
    @Min(value = 0, message = "Stock no puede ser negativo")
    Integer nuevoStock,

    @Size(max = 200, message = "Motivo máximo 200 caracteres")
    String motivo
) {}
