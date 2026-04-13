package com.antecsis.dto.sector;

import jakarta.validation.constraints.NotNull;

public record SectorActivoRequestDTO(
        @NotNull(message = "activo es obligatorio")
        Boolean activo
) {}
