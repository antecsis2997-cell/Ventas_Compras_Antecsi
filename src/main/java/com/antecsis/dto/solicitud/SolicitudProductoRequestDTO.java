package com.antecsis.dto.solicitud;

import com.antecsis.entity.EstadoSolicitud;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SolicitudProductoRequestDTO {
    @NotBlank(message = "Nombre del emisor es obligatorio")
    private String nombreEmisor;
    private Long productoId;
    private String descripcion;
    private BigDecimal precio;
    private EstadoSolicitud estado;
}
