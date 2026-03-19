package com.antecsis.dto.receta;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecetaDetalleRequestDTO(
        @NotNull(message = "insumoId es obligatorio")
        Long insumoId,

        @NotNull(message = "cantidadInsumoBase es obligatoria")
        @Min(value = 1, message = "cantidadInsumoBase debe ser mayor o igual a 1")
        Integer cantidadInsumoBase
) {
}

