package com.antecsis.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponseDTO {
    private Long productoId;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String moneda;
    private Integer stock;
    private String unidadMedida;
    private Integer stockMinimoAlerta;
    private Long sectorId;
    private String sectorNombre;
}
