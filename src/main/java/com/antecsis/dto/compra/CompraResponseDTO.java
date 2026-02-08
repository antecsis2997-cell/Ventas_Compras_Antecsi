package com.antecsis.dto.compra;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CompraResponseDTO {
    private Long id;
    private Long proveedorId;
    private String proveedorNombre;
    private LocalDateTime fecha;
    private Double total;
}
