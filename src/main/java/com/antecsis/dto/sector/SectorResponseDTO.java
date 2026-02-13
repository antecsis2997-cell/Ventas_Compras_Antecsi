package com.antecsis.dto.sector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SectorResponseDTO {
    private Long id;
    private String nombreSector;
    private String telefono;
    private String direccion;
}
