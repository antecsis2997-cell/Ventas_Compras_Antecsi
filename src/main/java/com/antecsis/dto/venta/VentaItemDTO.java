package com.antecsis.dto.venta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VentaItemDTO(
    @NotNull(message = "Producto es obligatorio")
    Long productoId,

    @NotNull(message = "Cantidad es obligatoria")
    @Min(value = 1, message = "Cantidad debe ser al menos 1")
    Integer cantidad,

    @DecimalMin(value = "0.01", message = "Precio unitario debe ser mayor a 0")
    BigDecimal precioUnitario
) {}
