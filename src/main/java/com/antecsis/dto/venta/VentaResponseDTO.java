package com.antecsis.dto.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class VentaResponseDTO {
    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private String usuarioNombre;
    private String metodoPagoNombre;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado;
    private String tipoDocumento;
    private String numeroDocumento;
    private String observaciones;
}
