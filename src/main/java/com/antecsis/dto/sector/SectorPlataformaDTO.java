package com.antecsis.dto.sector;

public record SectorPlataformaDTO(
        Long id,
        String nombreSector,
        String telefono,
        String direccion,
        String videoPromocionalUrl,
        boolean activo
) {}
