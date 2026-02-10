package com.antecsis.dto.proveedor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProveedorRequestDTO {
    @NotBlank(message = "Nombre es obligatorio")
    private String nombre;

    private String ruc;

    @Email(message = "Email inválido")
    private String email;

    private String telefono;

    private String direccion;
}
