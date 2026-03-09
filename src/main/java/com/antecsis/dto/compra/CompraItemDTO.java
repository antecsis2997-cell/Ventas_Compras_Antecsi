package com.antecsis.dto.compra;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CompraItemDTO(
    @NotNull(message = "Producto es obligatorio")
    Long productoId,

    @NotNull(message = "Cantidad es obligatoria")
    @Min(value = 1, message = "Cantidad debe ser al menos 1")
    Integer cantidad,

    @NotNull(message = "Precio unitario es obligatorio")
    @Positive(message = "Precio unitario debe ser positivo")
    BigDecimal precioUnitario
) {}
