package com.antecsis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InventarioResponseDTO {
    private Long productoId;
    private String nombre;
    private Integer stock;
}
