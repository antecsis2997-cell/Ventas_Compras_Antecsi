package com.antecsis.dto.localizacion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LocalizacionResponseDTO {
    private Long id;
    private String descripcionLocal;
    private String imagenUrl;
}
