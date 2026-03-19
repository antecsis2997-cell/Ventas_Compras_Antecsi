package com.antecsis.dto.conversion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConversionRequestDTO(
        @Schema(description = "ID de la receta (BOM)")
        @NotNull(message = "recetaId es obligatorio")
        Long recetaId,

        @Schema(description = "Cantidad de unidades a producir (debe ser múltiplo de la cantidadSalidaBase)")
        @NotNull(message = "cantidadProducir es obligatoria")
        @Min(value = 1, message = "cantidadProducir debe ser mayor o igual a 1")
        Integer cantidadProducir
) {
}

