package com.antecsis.dto.inventario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MovimientoResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private String tipo;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String motivo;
    private Long referenciaId;
    private String usuarioNombre;
    private LocalDateTime fecha;
}
