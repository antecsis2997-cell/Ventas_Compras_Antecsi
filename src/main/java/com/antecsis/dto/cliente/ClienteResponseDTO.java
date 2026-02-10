package com.antecsis.dto.cliente;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClienteResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String tipoDocumento;
    private String documento;
    private String direccion;
    private Boolean activo;
}
