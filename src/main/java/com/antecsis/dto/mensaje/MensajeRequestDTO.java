package com.antecsis.dto.mensaje;

import com.antecsis.entity.EstadoSolicitud;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record MensajeRequestDTO(
    @NotBlank(message = "Nombre del receptor es obligatorio")
    String nombreReceptor,

    String item,
    String descripcion,
    BigDecimal precio,
    EstadoSolicitud estado,
    String nombreEmisor
) {}
