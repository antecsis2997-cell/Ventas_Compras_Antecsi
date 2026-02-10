package com.antecsis.dto.producto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductoResponseDTO {
	private Long id;
	private String codigo;
	private String nombre;
	private String descripcion;
	private BigDecimal precio;
	private BigDecimal precioCompra;
	private Integer stock;
	private Long categoriaId;
	private String categoriaNombre;
	private Boolean activo;
}
