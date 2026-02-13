package com.antecsis.dto.sector;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectorRequestDTO {

    @NotBlank(message = "Nombre del sector es obligatorio")
    private String nombreSector;
    private String telefono;
    private String direccion;
}
