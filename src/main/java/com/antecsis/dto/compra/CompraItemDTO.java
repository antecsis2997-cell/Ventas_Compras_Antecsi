package com.antecsis.dto.compra;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompraItemDTO {
    @NotNull
    private Long productoId;

    @Min(1)
    private Integer cantidad;

    @Positive
    private Double precioUnitario;
}
