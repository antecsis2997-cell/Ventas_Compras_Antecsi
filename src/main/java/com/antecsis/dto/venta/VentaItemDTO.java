package com.antecsis.dto.venta;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VentaItemDTO {
    @NotNull
    private Long productoId;

    @Min(1)
    private Integer cantidad;
}
