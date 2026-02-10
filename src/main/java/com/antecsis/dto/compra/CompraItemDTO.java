package com.antecsis.dto.compra;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompraItemDTO {
    @NotNull(message = "Producto es obligatorio")
    private Long productoId;

    @NotNull(message = "Cantidad es obligatoria")
    @Min(value = 1, message = "Cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "Precio unitario es obligatorio")
    @Positive(message = "Precio unitario debe ser positivo")
    private BigDecimal precioUnitario;
}
