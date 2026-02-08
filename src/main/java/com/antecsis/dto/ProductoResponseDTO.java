package com.antecsis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductoResponseDTO {
	private Long id;
	private String nombre;
	private Double precio;
	private Integer stock;
}
