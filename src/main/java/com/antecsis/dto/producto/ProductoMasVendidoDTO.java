package com.antecsis.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductoMasVendidoDTO {
	private Long productoId;
    private String nombre;
    private Long cantidadVendida;
}
