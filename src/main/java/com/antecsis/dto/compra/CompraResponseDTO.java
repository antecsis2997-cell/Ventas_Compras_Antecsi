package com.antecsis.dto.compra;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CompraResponseDTO {
    private Long id;
    private Long proveedorId;
    private String proveedorNombre;
    private String usuarioNombre;
    private String metodoPagoNombre;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado;
    private String observaciones;
    private String numeroDocumento;
}
