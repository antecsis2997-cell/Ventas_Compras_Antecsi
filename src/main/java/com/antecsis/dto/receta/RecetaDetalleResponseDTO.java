package com.antecsis.dto.receta;

public record RecetaDetalleResponseDTO(
        Long insumoId,
        String insumoNombre,
        Integer cantidadInsumoBase
) {
}

