package com.antecsis.dto.sector;

import jakarta.validation.constraints.NotBlank;

public record SectorRequestDTO(
    @NotBlank(message = "Nombre del sector es obligatorio")
    String nombreSector,

    String telefono,
    String direccion,

    /** Prefijo/serie para boletas (ej. B137). Opcional; si está configurado el número se genera automáticamente. */
    String prefijoBoleta,

    /** Prefijo/serie para facturas (ej. F137). Opcional. */
    String prefijoFactura
) {}
