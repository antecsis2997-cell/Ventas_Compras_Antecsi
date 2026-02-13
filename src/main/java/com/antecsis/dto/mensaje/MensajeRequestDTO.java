package com.antecsis.dto.mensaje;

import com.antecsis.entity.EstadoSolicitud;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MensajeRequestDTO {
    @NotBlank(message = "Nombre del receptor es obligatorio")
    private String nombreReceptor;
    private String item;
    private String descripcion;
    private BigDecimal precio;
    private EstadoSolicitud estado;
    private String nombreEmisor;
}
