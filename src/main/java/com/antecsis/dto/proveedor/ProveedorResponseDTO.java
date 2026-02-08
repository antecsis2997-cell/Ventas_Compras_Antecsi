package com.antecsis.dto.proveedor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor
public class ProveedorResponseDTO {
	private Long id;
    private String nombre;
    private String telefono;
}
