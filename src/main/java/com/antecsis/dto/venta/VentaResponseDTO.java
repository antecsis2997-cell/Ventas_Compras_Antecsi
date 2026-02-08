package com.antecsis.dto.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class VentaResponseDTO {
    private Long id;
    private Long clienteId;
    private LocalDateTime fecha;
    private Double total;
}
