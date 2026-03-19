package com.antecsis.dto.receta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Receta (BOM) para producir un producto consumiendo insumos. Produce varias unidades escalando proporcionalmente.")
public record RecetaRequestDTO(
        @Schema(description = "Producto vendible que se producirá (no insumo)")
        @NotNull(message = "productoSalidaId es obligatorio")
        Long productoSalidaId,

        @Schema(description = "Cantidad base que produce la receta (para escala). Debe ser un entero > 0")
        @NotNull(message = "cantidadSalidaBase es obligatoria")
        @Min(value = 1, message = "cantidadSalidaBase debe ser mayor o igual a 1")
        Integer cantidadSalidaBase,

        @Schema(description = "Detalle de insumos consumidos por la receta")
        @NotEmpty(message = "Debe incluir al menos un insumo")
        List<@Valid RecetaDetalleRequestDTO> detalles
) {
}

