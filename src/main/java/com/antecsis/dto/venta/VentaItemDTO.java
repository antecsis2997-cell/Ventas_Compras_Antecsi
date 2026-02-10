package com.antecsis.dto.venta;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VentaItemDTO {
    @NotNull(message = "Producto es obligatorio")
    private Long productoId;

    @NotNull(message = "Cantidad es obligatoria")
    @Min(value = 1, message = "Cantidad debe ser al menos 1")
    private Integer cantidad;
}
