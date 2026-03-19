package com.antecsis.dto.receta;

import java.util.List;

public record RecetaResponseDTO(
        Long id,
        Long sectorId,
        Long productoSalidaId,
        String productoSalidaNombre,
        Integer cantidadSalidaBase,
        List<RecetaDetalleResponseDTO> detalles,
        Boolean activo
) {
}

