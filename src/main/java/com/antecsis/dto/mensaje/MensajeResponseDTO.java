package com.antecsis.dto.mensaje;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MensajeResponseDTO {
    private Long id;
    private String nombreReceptor;
    private String item;
    private String descripcion;
    private BigDecimal precio;
    private String estado;
    private LocalDateTime fecha;
    private String nombreEmisor;
}
