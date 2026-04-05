package com.antecsis.dto.sector;

import jakarta.validation.constraints.NotBlank;

public record SectorRequestDTO(
    @NotBlank(message = "Nombre del sector es obligatorio")
    String nombreSector,

    String telefono,
    String direccion,

    /** URL de video promocional (YouTube, Vimeo, etc.). */
    String videoPromocionalUrl
) {}
