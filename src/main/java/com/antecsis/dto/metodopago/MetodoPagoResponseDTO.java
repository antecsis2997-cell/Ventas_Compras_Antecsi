package com.antecsis.dto.metodopago;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MetodoPagoResponseDTO {
    private Long id;
    private String nombre;
    private Boolean activo;
}
