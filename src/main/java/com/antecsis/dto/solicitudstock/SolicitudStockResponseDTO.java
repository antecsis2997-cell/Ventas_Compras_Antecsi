package com.antecsis.dto.solicitudstock;

import java.time.LocalDateTime;

public record SolicitudStockResponseDTO(
    Long id,
    String nombre,
    String apellidos,
    String cargo,
    String asunto,
    String remitenteEmail,
    String nombreRemitente,
    Long productoId,
    String productoNombre,
    String unidadMedida,
    Integer cantidad,
    String estado,
    LocalDateTime fechaCreacion
) {}
