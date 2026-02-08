package com.antecsis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CompraResponseDTO {
    private Long id;
    private String proveedor;
    private LocalDateTime fecha;
    private Double total;
}
