package com.antecsis.dto.mensaje;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MensajeResponseDTO(
    Long id,
    String nombreReceptor,
    String item,
    String descripcion,
    BigDecimal precio,
    String estado,
    LocalDateTime fecha,
    String nombreEmisor
) {}
