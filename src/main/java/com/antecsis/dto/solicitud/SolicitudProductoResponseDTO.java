package com.antecsis.dto.solicitud;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitudProductoResponseDTO(
    Long id,
    String nombreEmisor,
    Long productoId,
    String productoNombre,
    String descripcion,
    BigDecimal precio,
    String estado,
    LocalDateTime fecha,
    Boolean atendida
) {}
