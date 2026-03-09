package com.antecsis.dto.solicitud;

import com.antecsis.entity.EstadoSolicitud;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record SolicitudProductoRequestDTO(
    @NotBlank(message = "Nombre del emisor es obligatorio")
    String nombreEmisor,

    Long productoId,
    String descripcion,
    BigDecimal precio,
    EstadoSolicitud estado
) {}
