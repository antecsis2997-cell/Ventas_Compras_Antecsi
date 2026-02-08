package com.antecsis.dto.proveedor;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class ProveedorRequestDTO {
	@NotBlank private String nombre;
    private String telefono;
}
