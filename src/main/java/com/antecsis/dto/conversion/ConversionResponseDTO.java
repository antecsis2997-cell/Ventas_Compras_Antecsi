package com.antecsis.dto.conversion;

import java.time.LocalDateTime;

public record ConversionResponseDTO(
        Long id,
        Long recetaId,
        Integer cantidadProducir,
        String productoSalidaNombre,
        LocalDateTime fecha,
        String estado
) {
}

