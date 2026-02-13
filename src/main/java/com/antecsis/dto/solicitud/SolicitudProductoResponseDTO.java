package com.antecsis.dto.solicitud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SolicitudProductoResponseDTO {
    private Long id;
    private String nombreEmisor;
    private Long productoId;
    private String productoNombre;
    private String descripcion;
    private BigDecimal precio;
    private String estado;
    private LocalDateTime fecha;
    private Boolean atendida;
}
